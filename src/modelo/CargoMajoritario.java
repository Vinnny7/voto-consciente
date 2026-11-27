package modelo;

import java.util.List;

/** Especialização de um cargo majoritário, como presidente. */
public final class CargoMajoritario extends Cargo {
    public CargoMajoritario(String id, String nome, int digitos, List<Candidato> candidatos) {
        super(id, nome, digitos, candidatos);
    }

    @Override
    public String getTipo() { return "Majoritário"; }
}
