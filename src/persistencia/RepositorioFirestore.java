package persistencia;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import modelo.SessaoVotacao;

public final class RepositorioFirestore implements RepositorioVotos {
    private static final URI API = URI.create("https://firestore.googleapis.com");
    private final String projeto;
    private final URI api;
    private final Credenciais credenciais;
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public RepositorioFirestore(String projeto) {
        this(projeto, API, RepositorioFirestore::obterToken);
    }

    RepositorioFirestore(String projeto, URI api, Credenciais credenciais) {
        this.projeto = projeto;
        this.api = api;
        this.credenciais = credenciais;
    }

    @Override
    public void salvar(UUID id, Map<SessaoVotacao.Cargo, String> votos)
            throws IOException, InterruptedException {
        String deputado = valorVoto(votos, SessaoVotacao.Cargo.DEPUTADO_FEDERAL);
        String presidente = valorVoto(votos, SessaoVotacao.Cargo.PRESIDENTE);
        String corpo = """
                {
                  "fields": {
                    "deputadoFederal": {"stringValue": "%s"},
                    "presidente": {"stringValue": "%s"}
                  }
                }
                """.formatted(deputado, presidente);

        // Repetir o envio atualiza o mesmo documento com as mesmas escolhas.
        URI documento = api.resolve("/v1/projects/" + projeto
                + "/databases/(default)/documents/votos/" + id);
        HttpRequest pedido = HttpRequest.newBuilder(documento)
                .header("Authorization", "Bearer " + credenciais.token())
                .header("X-Goog-User-Project", projeto)
                .header("Content-Type", "application/json; charset=UTF-8")
                .timeout(Duration.ofSeconds(15))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8))
                .build();

        HttpResponse<Void> resposta;
        try {
            resposta = cliente.send(pedido, HttpResponse.BodyHandlers.discarding());
        } catch (IOException erro) {
            throw new IOException("Confira a conexão e tente novamente.", erro);
        }
        int status = resposta.statusCode();
        if (status < 200 || status >= 300) {
            throw new IOException(switch (status) {
                case 401 -> "Refaça o login do Google Cloud.";
                case 403 -> "A conta precisa de acesso ao projeto do Firebase.";
                case 404 -> "Confira se o banco padrão do Firestore existe.";
                case 429 -> "Limite do Firestore atingido. Tente mais tarde.";
                default -> "O Firestore retornou o erro " + status + ".";
            });
        }
    }

    private static String valorVoto(Map<SessaoVotacao.Cargo, String> votos,
                                    SessaoVotacao.Cargo cargo) {
        String valor = votos.get(cargo);
        if ("BRANCO".equals(valor) || "NULO".equals(valor) || cargo.buscarCandidato(valor) != null) {
            return valor;
        }
        throw new IllegalArgumentException("Escolha inválida para " + cargo.getNome());
    }

    private static String obterToken() throws IOException, InterruptedException {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        ProcessBuilder comando = windows
                ? new ProcessBuilder("cmd.exe", "/d", "/c", "gcloud.cmd", "auth",
                        "application-default", "print-access-token", "--quiet")
                : new ProcessBuilder("gcloud", "auth", "application-default",
                        "print-access-token", "--quiet");
        comando.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process processo;
        try {
            processo = comando.start();
        } catch (IOException erro) {
            throw new IOException("Abra um terminal com o Google Cloud CLI instalado.", erro);
        }

        try (var saida = processo.getInputStream()) {
            if (!processo.waitFor(20, TimeUnit.SECONDS)) {
                throw new IOException("O login do Google Cloud demorou demais.");
            }
            String token = new String(saida.readAllBytes(), StandardCharsets.UTF_8).strip();
            if (processo.exitValue() != 0 || token.isBlank()) {
                throw new IOException("Confira o login do Google Cloud no terminal.");
            }
            return token;
        } finally {
            if (processo.isAlive()) {
                processo.destroyForcibly();
            }
        }
    }

    @FunctionalInterface
    interface Credenciais {
        String token() throws IOException, InterruptedException;
    }
}
