package configuracao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Lê a configuração local, dando prioridade às variáveis do ambiente. */
public final class ConfiguracaoEnv {
    private final Map<String, String> valores = new HashMap<>();

    public ConfiguracaoEnv(Path arquivo) throws IOException {
        if (Files.isRegularFile(arquivo)) {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                String texto = linha.replace("\uFEFF", "").strip();
                if (texto.isBlank() || texto.startsWith("#")) {
                    continue;
                }
                int posicao = texto.indexOf('=');
                if (posicao < 1) {
                    throw new IOException("Linha inválida no arquivo .env.");
                }
                String valor = texto.substring(posicao + 1).strip();
                if (valor.length() >= 2 && ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'")))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                valores.put(texto.substring(0, posicao).strip(), valor);
            }
        }
    }

    public String obter(String chave, String padrao) {
        String valor = System.getenv(chave);
        if (valor == null || valor.isBlank()) {
            valor = valores.getOrDefault(chave, padrao);
        }
        return valor.strip();
    }
}
