package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Consolida as escolhas lidas do Firestore para cada cargo. */
public final class RelatorioEleicao {
    private final String eleicaoId;
    private final List<Cargo> cargos;
    private final List<Map<String, String>> votacoes;

    public RelatorioEleicao(String eleicaoId, List<Cargo> cargos,
                             List<Map<String, String>> votacoes) {
        this.eleicaoId = eleicaoId;
        this.cargos = List.copyOf(cargos);
        this.votacoes = votacoes.stream().map(Map::copyOf).toList();
    }

    public String getEleicaoId() { return eleicaoId; }
    public List<Cargo> getCargos() { return cargos; }
    public long getTotalParticipantes() { return votacoes.size(); }

    public List<LinhaApuracao> getLinhas(Cargo cargo) {
        List<LinhaApuracao> linhas = new ArrayList<>();
        for (Candidato candidato : cargo.getCandidatos()) {
            linhas.add(linha(cargo, candidato.getNumero(),
                    candidato.getNome() + " (" + candidato.getNumero() + ")"));
        }
        linhas.add(linha(cargo, "BRANCO", "Votos Brancos"));
        linhas.add(linha(cargo, "NULO", "Votos Nulos"));
        return List.copyOf(linhas);
    }

    private LinhaApuracao linha(Cargo cargo, String valor, String descricao) {
        long quantidade = votacoes.stream()
                .filter(votos -> valor.equals(votos.get(cargo.getId())))
                .count();
        return new LinhaApuracao(descricao, quantidade, votacoes.size());
    }
}
