package modelo;

import java.util.Locale;

/** Uma linha do relatório, com percentual calculado sobre todos os votos do cargo. */
public final class LinhaApuracao {
    private final String descricao;
    private final long quantidade;
    private final long total;

    public LinhaApuracao(String descricao, long quantidade, long total) {
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.total = total;
    }

    public String getDescricao() { return descricao; }
    public long getQuantidade() { return quantidade; }

    public double getPercentual() {
        return total == 0 ? 0.0 : quantidade * 100.0 / total;
    }

    public String getTexto() {
        String percentual = String.format(Locale.forLanguageTag("pt-BR"), "%.2f", getPercentual());
        String unidade = quantidade == 1 ? " voto)" : " votos)";
        return descricao + ": " + percentual + "% (" + quantidade + unidade;
    }
}
