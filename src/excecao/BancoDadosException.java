package excecao;

/** Falha recuperável de comunicação, autenticação ou leitura do Firestore. */
public final class BancoDadosException extends Exception {
    public BancoDadosException(String mensagem) { super(mensagem); }
    public BancoDadosException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
