package controle;

import excecao.BancoDadosException;
import excecao.EleicaoException;
import java.util.List;
import java.util.UUID;
import modelo.Cargo;
import modelo.EstadoEleicao;
import modelo.RelatorioEleicao;
import modelo.SessaoVotacao;
import persistencia.RepositorioVotos;

/** Aplica as regras da eleição e coordena modelo, persistência e interface. */
public final class EleicaoController {
    private static final String SENHA_MESARIO = "99999";
    private final List<Cargo> cargos;
    private final RepositorioVotos repositorio;
    private final String eleicaoId = UUID.randomUUID().toString();
    private volatile EstadoEleicao estado = EstadoEleicao.AGUARDANDO_ABERTURA;
    private volatile SessaoVotacao sessao;
    private volatile boolean votosSalvos;

    public EleicaoController(List<Cargo> cargos, RepositorioVotos repositorio) {
        this.cargos = List.copyOf(cargos);
        this.repositorio = repositorio;
    }

    public EstadoEleicao getEstado() { return estado; }
    public List<Cargo> getCargos() { return cargos; }
    public String getEleicaoId() { return eleicaoId; }
    public SessaoVotacao getSessao() { return sessao; }
    public boolean isVotosSalvos() { return votosSalvos; }

    public synchronized RelatorioEleicao abrirEleicao(String senha)
            throws EleicaoException, BancoDadosException {
        validarSenha(senha);
        if (estado != EstadoEleicao.AGUARDANDO_ABERTURA) {
            throw new EleicaoException("A eleição já foi aberta.");
        }
        repositorio.abrirEleicao(eleicaoId);
        var votos = repositorio.listarVotacoes(eleicaoId);
        if (!votos.isEmpty()) {
            throw new EleicaoException("A eleição precisa iniciar com a zerésima vazia.");
        }
        sessao = new SessaoVotacao(cargos);
        estado = EstadoEleicao.EM_VOTACAO;
        return new RelatorioEleicao(eleicaoId, cargos, votos);
    }

    public synchronized void confirmarVoto(String escolha)
            throws EleicaoException, BancoDadosException {
        exigirVotacaoAberta();
        if (sessao.estaEncerrada()) {
            throw new EleicaoException("Inicie uma nova votação ou reenvie a votação pendente.");
        }
        sessao.confirmarVoto(escolha);
        if (sessao.estaEncerrada()) {
            salvarVotacaoPendente();
        }
    }

    public synchronized void salvarVotacaoPendente()
            throws EleicaoException, BancoDadosException {
        exigirVotacaoAberta();
        if (!sessao.estaEncerrada()) {
            throw new EleicaoException("Confirme os dois cargos antes de salvar.");
        }
        if (!votosSalvos) {
            repositorio.salvarVotacao(eleicaoId, sessao.getId(), sessao.getVotos());
            votosSalvos = true;
        }
    }

    public synchronized void iniciarNovaVotacao() throws EleicaoException {
        exigirVotacaoAberta();
        if (!votosSalvos) {
            throw new EleicaoException("Conclua e salve a votação atual antes de iniciar outra.");
        }
        sessao = new SessaoVotacao(cargos);
        votosSalvos = false;
    }

    public synchronized RelatorioEleicao encerrarEleicao(String senha)
            throws EleicaoException, BancoDadosException {
        validarSenha(senha);
        if (estado != EstadoEleicao.EM_VOTACAO && estado != EstadoEleicao.ENCERRAMENTO_PENDENTE) {
            throw new EleicaoException("A eleição precisa estar aberta para encerrar.");
        }
        if (sessao.estaIniciada() && !votosSalvos) {
            throw new EleicaoException("Conclua e salve os dois cargos antes de encerrar.");
        }
        estado = EstadoEleicao.ENCERRAMENTO_PENDENTE;
        repositorio.encerrarEleicao(eleicaoId);
        var votos = repositorio.listarVotacoes(eleicaoId);
        estado = EstadoEleicao.ENCERRADA;
        return new RelatorioEleicao(eleicaoId, cargos, votos);
    }

    private void exigirVotacaoAberta() throws EleicaoException {
        if (estado != EstadoEleicao.EM_VOTACAO) {
            throw new EleicaoException("A urna está bloqueada. Confira o estado da eleição.");
        }
    }

    private void validarSenha(String senha) throws EleicaoException {
        if (!SENHA_MESARIO.equals(senha)) {
            throw new EleicaoException("Senha inválida.");
        }
    }
}
