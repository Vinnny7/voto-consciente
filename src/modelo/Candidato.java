package modelo;

/** Candidato fictício exibido pela urna após a digitação completa. */
public final class Candidato {
    private final String numero;
    private final String nome;
    private final String partido;
    private final String foto;

    public Candidato(String numero, String nome, String partido) {
        this(numero, nome, partido, "");
    }

    public Candidato(String numero, String nome, String partido, String foto) {
        this.numero = numero;
        this.nome = nome;
        this.partido = partido;
        this.foto = foto;
    }

    public String getNumero() { return numero; }
    public String getNome() { return nome; }
    public String getPartido() { return partido; }
    public String getFoto() { return foto; }
}
