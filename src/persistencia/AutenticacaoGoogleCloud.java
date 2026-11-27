package persistencia;

import excecao.BancoDadosException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/** Usa o login local do Google Cloud CLI, com renovação do token de acesso. */
public final class AutenticacaoGoogleCloud implements FirestoreProxy.FornecedorToken {
    private String token;
    private long validoAte;

    @Override
    public synchronized String obter() throws BancoDadosException {
        if (token != null && System.nanoTime() < validoAte) {
            return token;
        }
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        ProcessBuilder comando = windows
                ? new ProcessBuilder("cmd.exe", "/d", "/c", "gcloud.cmd", "auth",
                        "application-default", "print-access-token", "--quiet")
                : new ProcessBuilder("gcloud", "auth", "application-default",
                        "print-access-token", "--quiet");
        comando.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process processo = null;
        try {
            processo = comando.start();
            if (!processo.waitFor(25, TimeUnit.SECONDS)) {
                throw new BancoDadosException("O Google Cloud demorou para responder. Tente novamente.");
            }
            String resposta = new String(processo.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8).strip();
            if (processo.exitValue() != 0 || resposta.isBlank()) {
                throw new BancoDadosException(
                        "Faça login com gcloud.cmd auth application-default login e tente novamente.");
            }
            token = resposta;
            validoAte = System.nanoTime() + TimeUnit.MINUTES.toNanos(45);
            return token;
        } catch (IOException erro) {
            throw new BancoDadosException("Confira a instalação do Google Cloud CLI.", erro);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new BancoDadosException("A autenticação foi interrompida.", erro);
        } finally {
            if (processo != null && processo.isAlive()) {
                processo.destroyForcibly();
            }
        }
    }

    @Override
    public synchronized void invalidar() {
        token = null;
        validoAte = 0;
    }
}
