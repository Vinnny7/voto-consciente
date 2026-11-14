public final class Candidato {
    private final String numero;
    private final String nome;
    private final String partido;

    public Candidato(String numero, String nome, String partido) {
        this.numero = numero;
        this.nome = nome;
        this.partido = partido;
    }

    public String getNumero() {
        return numero;
    }

    public String getNome() {
        return nome;
    }

    public String getPartido() {
        return partido;
    }

    public static void main(String[] args) {
        Candidato candidato = new Candidato("9202", "Lucas do Sol", "Partido do Sol");

        System.out.println("Número: " + candidato.getNumero());
        System.out.println("Nome: " + candidato.getNome());
        System.out.println("Partido: " + candidato.getPartido());
    }
}
