package modelo;

import java.util.List;

/** Especialização de um cargo proporcional, como deputado federal. */
public final class CargoProporcional extends Cargo {
    public CargoProporcional(String id, String nome, int digitos, List<Candidato> candidatos) {
        super(id, nome, digitos, candidatos);
    }

    @Override
    public String getTipo() { return "Proporcional"; }
}
