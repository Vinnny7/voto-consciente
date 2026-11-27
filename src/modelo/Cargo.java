package modelo;

import java.util.List;

/** Características comuns aos cargos proporcionais e majoritários. */
public abstract class Cargo {
    private final String id;
    private final String nome;
    private final int quantidadeDigitos;
    private final List<Candidato> candidatos;

    protected Cargo(String id, String nome, int quantidadeDigitos, List<Candidato> candidatos) {
        this.id = id;
        this.nome = nome;
        this.quantidadeDigitos = quantidadeDigitos;
        this.candidatos = List.copyOf(candidatos);
    }

    public abstract String getTipo();
    public String getId() { return id; }
    public String getNome() { return nome; }
    public int getQuantidadeDigitos() { return quantidadeDigitos; }
    public List<Candidato> getCandidatos() { return candidatos; }

    public Candidato buscarCandidato(String numero) {
        for (Candidato candidato : candidatos) {
            if (candidato.getNumero().equals(numero)) {
                return candidato;
            }
        }
        return null;
    }
}
