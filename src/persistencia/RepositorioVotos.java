package persistencia;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import modelo.SessaoVotacao;

public interface RepositorioVotos {
    void salvar(UUID id, Map<SessaoVotacao.Cargo, String> votos)
            throws IOException, InterruptedException;
}
