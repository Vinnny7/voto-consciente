package visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import modelo.Candidato;
import modelo.SessaoVotacao;
import persistencia.RepositorioFirestore;
import persistencia.RepositorioVotos;

public final class TelaUrna extends JPanel {
    private static final String PROJETO_FIREBASE = "voto-consciente-b1b46";
    private static final Color FUNDO = new Color(0xF6F7F2);
    private static final Color TEXTO = new Color(0x192925);
    private static final Color SECUNDARIO = new Color(0x54665E);
    private static final Color VERDE = new Color(0x126045);
    private static final Color PAINEL = new Color(0xE7EDE3);
    private static final Color BORDA = new Color(0xDCE4D9);

    private final RepositorioVotos repositorio;
    private SessaoVotacao sessao = new SessaoVotacao();
    private UUID idSessao = UUID.randomUUID();
    private final StringBuilder digitos = new StringBuilder();
    private boolean brancoSelecionado;
    private boolean salvando;
    private String erroSalvamento;

    private final JLabel etapa = rotulo("", 14, Font.BOLD, VERDE);
    private final JLabel cargo = rotulo("", 30, Font.BOLD, TEXTO);
    private final JLabel numero = rotulo("", 34, Font.BOLD, VERDE);
    private final JLabel escolha = rotulo("", 21, Font.BOLD, TEXTO);
    private final JLabel partido = rotulo("", 15, Font.PLAIN, SECUNDARIO);
    private final JLabel cargoLista = rotulo("", 14, Font.PLAIN, SECUNDARIO);
    private final JPanel candidatos = new JPanel();
    private final JButton[] teclas = new JButton[10];
    private final JButton branco = botao("BRANCO", Color.WHITE, TEXTO);
    private final JButton corrige = botao("CORRIGE", new Color(0xF6BC77), TEXTO);
    private final JButton confirma = botao("CONFIRMA", VERDE, Color.WHITE);
    private final JButton reiniciar = botao("Nova votação", VERDE, Color.WHITE);

    public TelaUrna() {
        this(new RepositorioFirestore(PROJETO_FIREBASE));
    }

    TelaUrna(RepositorioVotos repositorio) {
        super(new BorderLayout(12, 22));
        this.repositorio = repositorio;
        setBackground(FUNDO);
        setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JPanel cabecalho = coluna(FUNDO);
        cabecalho.add(rotulo("Voto Consciente", 34, Font.BOLD, TEXTO));
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(rotulo("Simulação educativa com candidatos fictícios", 15, Font.PLAIN, SECUNDARIO));
        add(cabecalho, BorderLayout.NORTH);

        JPanel urna = new JPanel(new BorderLayout(12, 0));
        urna.setOpaque(false);
        urna.add(montarVisor(), BorderLayout.CENTER);
        urna.add(montarTeclado(), BorderLayout.EAST);
        add(urna, BorderLayout.CENTER);
        add(montarLista(), BorderLayout.EAST);
        atualizarTela();
    }

    private JPanel montarVisor() {
        JPanel visor = new JPanel(new BorderLayout(0, 16));
        visor.setBackground(Color.WHITE);
        visor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)));
        visor.add(etapa, BorderLayout.NORTH);

        JPanel dados = coluna(Color.WHITE);
        dados.add(cargo);
        dados.add(Box.createVerticalStrut(14));
        numero.setFont(new Font(Font.MONOSPACED, Font.BOLD, 34));
        dados.add(numero);
        dados.add(Box.createVerticalStrut(14));
        dados.add(escolha);
        dados.add(Box.createVerticalStrut(4));
        dados.add(partido);
        visor.add(dados, BorderLayout.CENTER);

        reiniciar.setPreferredSize(new Dimension(150, 40));
        reiniciar.addActionListener(evento -> {
            if (erroSalvamento != null) {
                salvarVotacao();
                return;
            }
            sessao = new SessaoVotacao();
            idSessao = UUID.randomUUID();
            limparEscolha();
        });
        visor.add(reiniciar, BorderLayout.SOUTH);
        return visor;
    }

    private JPanel montarTeclado() {
        JPanel controles = new JPanel(new BorderLayout(0, 10));
        controles.setBackground(PAINEL);
        controles.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        controles.setPreferredSize(new Dimension(270, 320));

        JPanel numeros = new JPanel(new GridLayout(4, 3, 7, 7));
        numeros.setOpaque(false);
        for (int digito = 1; digito <= 9; digito++) {
            numeros.add(criarTecla(digito));
        }
        numeros.add(Box.createGlue());
        numeros.add(criarTecla(0));
        numeros.add(Box.createGlue());
        controles.add(numeros, BorderLayout.CENTER);

        JPanel acoes = new JPanel(new GridLayout(1, 3, 5, 0));
        acoes.setOpaque(false);
        acoes.setPreferredSize(new Dimension(246, 48));
        acoes.add(branco);
        acoes.add(corrige);
        acoes.add(confirma);
        controles.add(acoes, BorderLayout.SOUTH);

        branco.addActionListener(evento -> {
            digitos.setLength(0);
            brancoSelecionado = true;
            atualizarTela();
        });
        corrige.addActionListener(evento -> limparEscolha());
        confirma.addActionListener(evento -> {
            sessao.confirmarVoto(brancoSelecionado ? "BRANCO" : digitos.toString());
            if (sessao.estaEncerrada()) {
                salvarVotacao();
            } else {
                limparEscolha();
            }
        });
        return controles;
    }

    private JButton criarTecla(int digito) {
        JButton tecla = botao(Integer.toString(digito), TEXTO, Color.WHITE);
        tecla.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        tecla.addActionListener(evento -> {
            digitos.append(digito);
            atualizarTela();
        });
        teclas[digito] = tecla;
        return tecla;
    }

    private JPanel montarLista() {
        JPanel lista = coluna(Color.WHITE);
        lista.setPreferredSize(new Dimension(200, 320));
        lista.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        lista.add(rotulo("Candidatos fictícios", 16, Font.BOLD, TEXTO));
        lista.add(Box.createVerticalStrut(5));
        lista.add(cargoLista);
        lista.add(Box.createVerticalStrut(12));
        candidatos.setLayout(new BoxLayout(candidatos, BoxLayout.Y_AXIS));
        candidatos.setOpaque(false);
        candidatos.setAlignmentX(LEFT_ALIGNMENT);
        lista.add(candidatos);
        lista.add(Box.createVerticalGlue());
        return lista;
    }

    private void limparEscolha() {
        digitos.setLength(0);
        brancoSelecionado = false;
        atualizarTela();
    }

    private void salvarVotacao() {
        salvando = true;
        erroSalvamento = null;
        atualizarTela();
        UUID id = idSessao;
        var votos = sessao.getVotos();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                repositorio.salvar(id, votos);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (ExecutionException erro) {
                    erroSalvamento = erro.getCause().getMessage();
                    if (erroSalvamento == null) {
                        erroSalvamento = "Confira a conexão e tente novamente.";
                    }
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    erroSalvamento = "Envio interrompido. Tente novamente.";
                }
                salvando = false;
                atualizarTela();
            }
        }.execute();
    }

    private void atualizarTela() {
        boolean encerrada = sessao.estaEncerrada();
        SessaoVotacao.Cargo cargoAtual = encerrada ? null : sessao.getCargoAtual();
        int limite = encerrada ? 0 : cargoAtual.getQuantidadeDigitos();
        boolean completo = !encerrada && digitos.length() == limite;
        reiniciar.setVisible(encerrada && !salvando);
        reiniciar.setText(erroSalvamento == null ? "Nova votação" : "Tentar novamente");
        for (JButton tecla : teclas) {
            tecla.setEnabled(!encerrada && !completo && !brancoSelecionado);
        }
        branco.setEnabled(!encerrada && !brancoSelecionado);
        corrige.setEnabled(!encerrada && (!digitos.isEmpty() || brancoSelecionado));
        confirma.setEnabled(!encerrada && (completo || brancoSelecionado));
        partido.setText(" ");
        if (encerrada) {
            etapa.setText("Votação");
            numero.setText(" ");
            if (salvando) {
                cargo.setText("Salvando…");
                escolha.setText("Aguarde a confirmação.");
            } else if (erroSalvamento != null) {
                cargo.setText("Votação pendente");
                escolha.setText("Falha ao salvar.");
                partido.setText(erroSalvamento);
                partido.setToolTipText(erroSalvamento);
            } else {
                cargo.setText("FIM");
                escolha.setText("Votos salvos no Firestore.");
            }
            atualizarLista(null);
            return;
        }

        partido.setToolTipText(null);
        etapa.setText(cargoAtual == SessaoVotacao.Cargo.DEPUTADO_FEDERAL
                ? "Cargo 1 de 2" : "Cargo 2 de 2");
        cargo.setText(cargoAtual.getNome());
        String visor = digitos + "_".repeat(limite - digitos.length());
        numero.setText(brancoSelecionado ? " " : String.join(" ", visor.split("")));
        Candidato candidato = cargoAtual.buscarCandidato(digitos.toString());
        if (brancoSelecionado) {
            escolha.setText("Voto em branco");
        } else if (candidato != null) {
            escolha.setText(candidato.getNome());
            partido.setText(candidato.getPartido());
        } else if (completo) {
            escolha.setText("Voto nulo");
        } else {
            escolha.setText("Digite o número");
        }
        atualizarLista(cargoAtual);
    }

    private void atualizarLista(SessaoVotacao.Cargo cargoAtual) {
        candidatos.removeAll();
        if (cargoAtual == null) {
            cargoLista.setText(" ");
        } else {
            cargoLista.setText(cargoAtual.getNome());
            for (Candidato candidato : cargoAtual.getCandidatos()) {
                candidatos.add(rotulo(candidato.getNumero(), 20, Font.BOLD, VERDE));
                candidatos.add(rotulo(candidato.getNome(), 15, Font.BOLD, TEXTO));
                candidatos.add(rotulo(candidato.getPartido(), 13, Font.PLAIN, SECUNDARIO));
                candidatos.add(Box.createVerticalStrut(13));
            }
        }
        candidatos.revalidate();
        candidatos.repaint();
    }

    private static JPanel coluna(Color fundo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(fundo);
        return painel;
    }

    private static JLabel rotulo(String texto, int tamanho, int estilo, Color cor) {
        JLabel label = new JLabel(texto, SwingConstants.LEFT);
        label.setFont(new Font(Font.SANS_SERIF, estilo, tamanho));
        label.setForeground(cor);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getFontMetrics(label.getFont()).getHeight()));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JButton botao(String texto, Color fundo, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        botao.setForeground(cor);
        botao.setBackground(fundo);
        botao.setBorder(BorderFactory.createLineBorder(fundo.darker()));
        botao.setOpaque(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Voto Consciente");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setContentPane(new TelaUrna());
            janela.setMinimumSize(new Dimension(890, 470));
            janela.setSize(980, 525);
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
