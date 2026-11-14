import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SessaoVotacao {

    public enum Cargo {
        DEPUTADO_FEDERAL("Deputado federal", 4, List.of(
                new Candidato("9101", "Ana do Ipê", "Partido do Ipê"),
                new Candidato("9202", "Lucas do Sol", "Partido do Sol"))),
        PRESIDENTE("Presidente", 2, List.of(
                new Candidato("91", "Marina do Ipê", "Partido do Ipê"),
                new Candidato("92", "Rafael do Sol", "Partido do Sol")));

        private final String nome;
        private final int quantidadeDigitos;
        private final List<Candidato> candidatos;

        Cargo(String nome, int quantidadeDigitos, List<Candidato> candidatos) {
            this.nome = nome;
            this.quantidadeDigitos = quantidadeDigitos;
            this.candidatos = candidatos;
        }

        public String getNome() {
            return nome;
        }

        public int getQuantidadeDigitos() {
            return quantidadeDigitos;
        }

        public List<Candidato> getCandidatos() {
            return candidatos;
        }

        public Candidato buscarCandidato(String numero) {
            for (Candidato candidato : candidatos) {
                if (candidato.getNumero().equals(numero)) {
                    return candidato;
                }
            }
            return null;
        }
    }

    private final Map<Cargo, String> votos = new EnumMap<>(Cargo.class);
    private Cargo cargoAtual = Cargo.DEPUTADO_FEDERAL;

    public Cargo getCargoAtual() {
        if (estaEncerrada()) {
            throw new IllegalStateException("A sessão de votação já foi encerrada.");
        }
        return cargoAtual;
    }

    public boolean estaEncerrada() {
        return votos.size() == Cargo.values().length;
    }

    public Map<Cargo, String> getVotos() {
        return Map.copyOf(votos);
    }

    public void confirmarVoto(String escolha) {
        Cargo cargo = getCargoAtual();

        if (escolha == null || (!escolha.equals("BRANCO")
                && !escolha.matches("[0-9]{" + cargo.quantidadeDigitos + "}"))) {
            throw new IllegalArgumentException(
                    "Digite " + cargo.quantidadeDigitos + " números ou escolha BRANCO.");
        }

        String voto = escolha;
        if (!escolha.equals("BRANCO") && cargo.buscarCandidato(escolha) == null) {
            voto = "NULO";
        }

        votos.put(cargo, voto);
        if (cargo == Cargo.DEPUTADO_FEDERAL) {
            cargoAtual = Cargo.PRESIDENTE;
        }
    }

    public static void main(String[] args) {
        SessaoVotacao sessao = new SessaoVotacao();

        System.out.println("Primeiro cargo: " + sessao.getCargoAtual().getNome());
        sessao.confirmarVoto("9202");

        System.out.println("Segundo cargo: " + sessao.getCargoAtual().getNome());
        sessao.confirmarVoto("BRANCO");

        System.out.println("Deputado federal: " + sessao.getVotos().get(Cargo.DEPUTADO_FEDERAL));
        System.out.println("Presidente: " + sessao.getVotos().get(Cargo.PRESIDENTE));
        System.out.println("Sessão encerrada: " + sessao.estaEncerrada());
    }
}
