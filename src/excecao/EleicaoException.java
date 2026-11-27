package excecao;

/** Violação de uma regra da eleição simulada. */
public final class EleicaoException extends Exception {
    public EleicaoException(String mensagem) { super(mensagem); }
}
