package persistencia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import excecao.BancoDadosException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Proxy de acesso ao Firestore. Encapsula autenticação, JSON e requisições REST. */
public final class FirestoreProxy implements RepositorioVotos {
    private final String projeto;
    private final String base;
    private final URI api;
    private final FornecedorToken autenticacao;
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    public FirestoreProxy(String projeto, String banco, FornecedorToken autenticacao) {
        this(projeto, banco, autenticacao, URI.create("https://firestore.googleapis.com"));
    }

    FirestoreProxy(String projeto, String banco, FornecedorToken autenticacao, URI api) {
        if (!projeto.matches("[a-z][a-z0-9-]{4,61}[a-z0-9]")) {
            throw new IllegalArgumentException("Confira FIREBASE_PROJECT_ID no arquivo .env.");
        }
        if (!banco.matches("[a-zA-Z0-9()._-]+")) {
            throw new IllegalArgumentException("Confira FIREBASE_DATABASE_ID no arquivo .env.");
        }
        this.projeto = projeto;
        this.base = "/v1/projects/" + projeto + "/databases/" + banco + "/documents";
        this.autenticacao = autenticacao;
        this.api = api;
    }

    @Override
    public void abrirEleicao(String eleicaoId) throws BancoDadosException {
        gravarEstado(eleicaoId, "ABERTA");
    }

    @Override
    public void encerrarEleicao(String eleicaoId) throws BancoDadosException {
        gravarEstado(eleicaoId, "ENCERRADA");
    }

    @Override
    public void salvarVotacao(String eleicaoId, String votacaoId, Map<String, String> votos)
            throws BancoDadosException {
        Map<String, String> dados = Map.copyOf(votos);
        if (dados.size() != 2 || !dados.keySet().equals(Set.of("deputadoFederal", "presidente"))) {
            throw new BancoDadosException("A votação precisa conter os dois cargos.");
        }
        // O mesmo ID mantém um único documento mesmo após uma resposta perdida.
        enviar("PATCH", base + "/eleicoes/" + segmento(eleicaoId)
                + "/votos/" + segmento(votacaoId), documento(dados));
    }

    @Override
    public List<Map<String, String>> listarVotacoes(String eleicaoId) throws BancoDadosException {
        List<Map<String, String>> votos = new ArrayList<>();
        String proximaPagina = "";
        Set<String> paginasRecebidas = new HashSet<>();
        do {
            String caminho = base + "/eleicoes/" + segmento(eleicaoId) + "/votos?pageSize=200";
            if (!proximaPagina.isEmpty()) {
                caminho += "&pageToken=" + URLEncoder.encode(proximaPagina, StandardCharsets.UTF_8);
            }
            JsonObject resposta = enviar("GET", caminho, null);
            JsonArray documentos = resposta.getAsJsonArray("documents");
            if (documentos != null) {
                for (JsonElement elemento : documentos) {
                    JsonObject campos = elemento.getAsJsonObject().getAsJsonObject("fields");
                    if (campos == null) {
                        throw new BancoDadosException("O Firestore retornou uma votação incompleta.");
                    }
                    Map<String, String> escolhas = new LinkedHashMap<>();
                    for (String cargo : List.of("deputadoFederal", "presidente")) {
                        JsonObject campo = campos.getAsJsonObject(cargo);
                        if (campo == null || !campo.has("stringValue")) {
                            throw new BancoDadosException("O Firestore retornou uma votação incompleta.");
                        }
                        String valor = campo.get("stringValue").getAsString();
                        boolean valido = cargo.equals("deputadoFederal")
                                ? Set.of("9101", "9202", "BRANCO", "NULO").contains(valor)
                                : Set.of("91", "92", "BRANCO", "NULO").contains(valor);
                        if (!valido) {
                            throw new BancoDadosException("Há uma escolha inválida no banco desta eleição.");
                        }
                        escolhas.put(cargo, valor);
                    }
                    votos.add(Map.copyOf(escolhas));
                }
            }
            proximaPagina = resposta.has("nextPageToken")
                    ? resposta.get("nextPageToken").getAsString() : "";
            if (!proximaPagina.isEmpty() && !paginasRecebidas.add(proximaPagina)) {
                throw new BancoDadosException("Falha ao percorrer as páginas do Firestore.");
            }
        } while (!proximaPagina.isEmpty());
        return List.copyOf(votos);
    }

    private void gravarEstado(String id, String estado) throws BancoDadosException {
        enviar("PATCH", base + "/eleicoes/" + segmento(id), documento(Map.of("estado", estado)));
    }

    private JsonObject documento(Map<String, String> valores) {
        JsonObject campos = new JsonObject();
        valores.forEach((nome, valor) -> {
            JsonObject campo = new JsonObject();
            campo.addProperty("stringValue", valor);
            campos.add(nome, campo);
        });
        JsonObject documento = new JsonObject();
        documento.add("fields", campos);
        return documento;
    }

    private String segmento(String id) {
        if (!id.matches("[a-f0-9-]{36}")) {
            throw new IllegalArgumentException("Identificador de eleição ou votação inválido.");
        }
        return id;
    }

    private JsonObject enviar(String metodo, String caminho, JsonObject corpo)
            throws BancoDadosException {
        try {
            HttpResponse<String> resposta = requisitar(metodo, caminho, corpo);
            if (resposta.statusCode() == 401) {
                autenticacao.invalidar();
                resposta = requisitar(metodo, caminho, corpo);
            }
            int codigo = resposta.statusCode();
            if (codigo < 200 || codigo >= 300) {
                throw new BancoDadosException(switch (codigo) {
                    case 401 -> "Refaça o login do Google Cloud.";
                    case 403 -> "A conta precisa de acesso ao projeto Firebase.";
                    case 404 -> "Confira o projeto e o banco (default) do Firestore.";
                    case 429 -> "Limite do Firestore atingido. Aguarde e tente novamente.";
                    default -> "Falha de comunicação com o Firestore (HTTP " + codigo + ").";
                });
            }
            return JsonParser.parseString(resposta.body()).getAsJsonObject();
        } catch (IOException erro) {
            throw new BancoDadosException("Confira a internet e tente novamente.", erro);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new BancoDadosException("A comunicação com o Firestore foi interrompida.", erro);
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException | NullPointerException erro) {
            throw new BancoDadosException("O Firestore retornou dados que a urna não conseguiu ler.", erro);
        }
    }

    private HttpResponse<String> requisitar(String metodo, String caminho, JsonObject corpo)
            throws IOException, InterruptedException, BancoDadosException {
        HttpRequest pedido = HttpRequest.newBuilder(api.resolve(caminho))
                .header("Authorization", "Bearer " + autenticacao.obter())
                .header("X-Goog-User-Project", projeto)
                .header("Content-Type", "application/json; charset=UTF-8")
                .timeout(Duration.ofSeconds(18))
                .method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(corpo.toString(), StandardCharsets.UTF_8))
                .build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /** Credencial fornecida pela autenticação local, substituível em testes. */
    public interface FornecedorToken {
        String obter() throws BancoDadosException;
        void invalidar();
    }
}
