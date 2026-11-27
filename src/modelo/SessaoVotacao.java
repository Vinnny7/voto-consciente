package modelo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Registra uma escolha por cargo para um participante da simulação. */
public final class SessaoVotacao {
    private final String id = UUID.randomUUID().toString();
    private final List<Cargo> cargos;
    private final Map<String, String> votos = new LinkedHashMap<>();
    private int indice;

    public SessaoVotacao(List<Cargo> cargos) {
        this.cargos = List.copyOf(cargos);
    }

    public String getId() { return id; }
    public Map<String, String> getVotos() { return Map.copyOf(votos); }
    public boolean estaEncerrada() { return indice == cargos.size(); }
    public boolean estaIniciada() { return indice > 0; }
    public int getIndiceCargo() { return indice; }

    public Cargo getCargoAtual() {
        if (estaEncerrada()) {
            throw new IllegalStateException("Os dois cargos já foram confirmados.");
        }
        return cargos.get(indice);
    }

    public void confirmarVoto(String escolha) {
        Cargo cargo = getCargoAtual();
        boolean branco = "BRANCO".equals(escolha);
        if (escolha == null || (!branco
                && !escolha.matches("[0-9]{" + cargo.getQuantidadeDigitos() + "}"))) {
            throw new IllegalArgumentException("Complete o número ou selecione BRANCO.");
        }
        String voto = branco ? "BRANCO" : "NULO";
        if (!branco && cargo.buscarCandidato(escolha) != null) {
            voto = escolha;
        }
        votos.put(cargo.getId(), voto);
        indice++;
    }
}
