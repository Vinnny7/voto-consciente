package persistencia;

import excecao.BancoDadosException;
import java.util.List;
import java.util.Map;

/** Abstração usada pelo controlador para persistir a eleição e seus votos. */
public interface RepositorioVotos {
    void abrirEleicao(String eleicaoId) throws BancoDadosException;
    void salvarVotacao(String eleicaoId, String votacaoId, Map<String, String> votos)
            throws BancoDadosException;
    List<Map<String, String>> listarVotacoes(String eleicaoId) throws BancoDadosException;
    void encerrarEleicao(String eleicaoId) throws BancoDadosException;
}
