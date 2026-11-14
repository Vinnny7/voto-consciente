import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public interface RepositorioVotos {
    void salvar(UUID id, Map<SessaoVotacao.Cargo, String> votos)
            throws IOException, InterruptedException;
}
