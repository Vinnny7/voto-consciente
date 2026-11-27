package visao;

import controle.EleicaoController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import modelo.Candidato;
import modelo.Cargo;
import modelo.EstadoEleicao;
import modelo.RelatorioEleicao;
import modelo.SessaoVotacao;

/** Interface Swing. Delega as regras da eleição e a persistência ao controlador. */
public class TelaUrna extends JPanel {
    private static final Color FUNDO = new Color(0xF6F7F2);
    private static final Color TEXTO = new Color(0x192925);
    private static final Color VERDE = new Color(0x126045);
    private static final Color SECUNDARIO = new Color(0x54665E);
    private static final Color BORDA = new Color(0xDCE4D9);

    private final EleicaoController controller;
    private final StringBuilder digitos = new StringBuilder();
    private final Map<String, ImageIcon> retratos = new HashMap<>();
    private boolean brancoSelecionado;
    private boolean ocupado;
    private RelatorioEleicao ultimoRelatorio;
    private String tituloRelatorio;

    private final JLabel etapa = rotulo("", 15, Font.BOLD, VERDE);
    private final JLabel cargo = rotulo("", 29, Font.BOLD, TEXTO);
    private final JLabel numero = rotulo("", 32, Font.BOLD, VERDE);
    private final JLabel nome = rotulo("", 23, Font.BOLD, TEXTO);
    private final JLabel partido = rotulo("", 16, Font.PLAIN, SECUNDARIO);
    private final JLabel foto = new JLabel();
    private final JLabel orientacao = rotulo("Abra a eleição para começar.", 15, Font.PLAIN, SECUNDARIO);
    private final JLabel estado = rotulo("", 15, Font.BOLD, VERDE);
    private final JPanel candidatos = new JPanel();
    private final JButton abrir = botao("Abrir eleição", VERDE, Color.WHITE);
    private final JButton encerrar = botao("Encerrar eleição", new Color(0xF6BC77), TEXTO);
    private final JButton relatorio = botao("Ver relatório", Color.WHITE, TEXTO);
    private final JButton branco = botao("BRANCO", Color.WHITE, TEXTO);
    private final JButton corrige = botao("CORRIGE", new Color(0xF6BC77), TEXTO);
    private final JButton confirma = botao("CONFIRMA", VERDE, Color.WHITE);
    private final JButton continuar = botao("Nova votação", VERDE, Color.WHITE);
    private final JButton[] teclas = new JButton[10];

    public TelaUrna(EleicaoController controller) {
        super(new BorderLayout(16, 16));
        this.controller = controller;
        setBackground(FUNDO);
        setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        add(montarCabecalho(), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(12, 0));
        centro.setOpaque(false);
        centro.add(montarVisor(), BorderLayout.CENTER);
        centro.add(montarTeclado(), BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);
        add(montarLista(), BorderLayout.EAST);
        add(orientacao, BorderLayout.SOUTH);
        carregarRetratos();
        atualizarTela();
    }

    private JPanel montarCabecalho() {
        JPanel painel = coluna(FUNDO);
        painel.add(rotulo("Voto Consciente", 32, Font.BOLD, TEXTO));
        painel.add(rotulo("Simulação educativa · candidatos e fotos fictícios",
                15, Font.PLAIN, SECUNDARIO));
        painel.add(Box.createVerticalStrut(14));
        JPanel mesario = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        mesario.setOpaque(false);
        mesario.setAlignmentX(LEFT_ALIGNMENT);
        mesario.add(abrir);
        mesario.add(encerrar);
        mesario.add(relatorio);
        mesario.add(estado);
        painel.add(mesario);
        abrir.addActionListener(evento -> {
            String senha = pedirSenha("Abrir eleição");
            if (senha != null) {
                executar(() -> controller.abrirEleicao(senha), resultado -> {
                    limparEscolha();
                    mostrarRelatorio("Zerésima", resultado, true);
                }, "Abrindo eleição e conferindo a zerésima…");
            }
        });
        encerrar.addActionListener(evento -> {
            String senha = pedirSenha("Encerrar eleição");
            if (senha != null) {
                executar(() -> controller.encerrarEleicao(senha),
                        resultado -> mostrarRelatorio("Apuração final", resultado, false),
                        "Encerrando eleição e lendo os votos do Firestore…");
            }
        });
        relatorio.addActionListener(evento -> {
            if (ultimoRelatorio != null) {
                TelaRelatorioEleicao.mostrar(this, tituloRelatorio, ultimoRelatorio, false);
            }
        });
        return painel;
    }

    private JPanel montarVisor() {
        JPanel visor = new JPanel(new BorderLayout(0, 16));
        visor.setBackground(Color.WHITE);
        visor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        visor.add(etapa, BorderLayout.NORTH);
        JPanel dados = coluna(Color.WHITE);
        dados.add(cargo);
        dados.add(Box.createVerticalStrut(10));
        numero.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
        dados.add(numero);
        dados.add(Box.createVerticalStrut(12));
        JPanel candidatura = new JPanel(new BorderLayout(12, 0));
        candidatura.setOpaque(false);
        candidatura.setAlignmentX(LEFT_ALIGNMENT);
        candidatura.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        JPanel textos = coluna(Color.WHITE);
        textos.add(nome);
        textos.add(Box.createVerticalStrut(6));
        textos.add(partido);
        foto.setPreferredSize(new Dimension(140, 160));
        foto.setHorizontalAlignment(JLabel.CENTER);
        candidatura.add(textos, BorderLayout.CENTER);
        candidatura.add(foto, BorderLayout.EAST);
        dados.add(candidatura);
        visor.add(dados, BorderLayout.CENTER);
        continuar.setPreferredSize(new Dimension(200, 42));
        continuar.addActionListener(evento -> {
            if (!controller.isVotosSalvos()) {
                executar(() -> {
                    controller.salvarVotacaoPendente();
                    return null;
                }, resultado -> limparEscolha(), "Reenviando a mesma votação…");
            } else {
                executar(() -> {
                    controller.iniciarNovaVotacao();
                    return null;
                }, resultado -> limparEscolha(), "Preparando a próxima votação…");
            }
        });
        visor.add(continuar, BorderLayout.SOUTH);
        return visor;
    }

    private JPanel montarTeclado() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(new Color(0xE7EDE3));
        painel.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        painel.setPreferredSize(new Dimension(280, 340));
        JPanel numeros = new JPanel(new GridLayout(4, 3, 7, 7));
        numeros.setOpaque(false);
        for (int digito = 1; digito <= 9; digito++) {
            numeros.add(criarTecla(digito));
        }
        numeros.add(Box.createGlue());
        numeros.add(criarTecla(0));
        numeros.add(Box.createGlue());
        painel.add(numeros, BorderLayout.CENTER);
        JPanel acoes = new JPanel(new GridLayout(1, 3, 5, 0));
        acoes.setOpaque(false);
        acoes.setPreferredSize(new Dimension(236, 48));
        acoes.add(branco);
        acoes.add(corrige);
        acoes.add(confirma);
        for (JButton acao : new JButton[] {branco, corrige, confirma}) {
            acao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            acao.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(acao.getBackground().darker()),
                    BorderFactory.createEmptyBorder(10, 3, 10, 3)));
        }
        painel.add(acoes, BorderLayout.SOUTH);
        branco.addActionListener(evento -> {
            digitos.setLength(0);
            brancoSelecionado = true;
            atualizarTela();
        });
        corrige.addActionListener(evento -> limparEscolha());
        confirma.addActionListener(evento -> {
            String escolha = brancoSelecionado ? "BRANCO" : digitos.toString();
            executar(() -> {
                controller.confirmarVoto(escolha);
                return null;
            }, resultado -> limparEscolha(), "Confirmando e salvando a votação…");
        });
        return painel;
    }

    private JButton criarTecla(int digito) {
        JButton tecla = botao(Integer.toString(digito), TEXTO, Color.WHITE);
        tecla.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        tecla.addActionListener(evento -> {
            digitos.append(digito);
            atualizarTela();
        });
        teclas[digito] = tecla;
        return tecla;
    }

    private JPanel montarLista() {
        JPanel lista = coluna(Color.WHITE);
        lista.setPreferredSize(new Dimension(190, 340));
        lista.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(15, 12, 15, 12)));
        lista.add(rotulo("Candidatos fictícios", 16, Font.BOLD, TEXTO));
        lista.add(Box.createVerticalStrut(15));
        candidatos.setLayout(new BoxLayout(candidatos, BoxLayout.Y_AXIS));
        candidatos.setOpaque(false);
        candidatos.setAlignmentX(LEFT_ALIGNMENT);
        lista.add(candidatos);
        return lista;
    }

    private void carregarRetratos() {
        for (Cargo cargoAtual : controller.getCargos()) {
            for (Candidato candidato : cargoAtual.getCandidatos()) {
                var recurso = getClass().getResource(candidato.getFoto());
                if (recurso == null) {
                    throw new IllegalStateException("Foto ausente no aplicativo: " + candidato.getFoto());
                }
                Image imagem = new ImageIcon(recurso).getImage()
                        .getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                retratos.put(candidato.getFoto(), new ImageIcon(imagem));
            }
        }
    }

    private void limparEscolha() {
        digitos.setLength(0);
        brancoSelecionado = false;
        atualizarTela();
    }

    private void atualizarTela() {
        EstadoEleicao situacao = controller.getEstado();
        SessaoVotacao sessao = controller.getSessao();
        boolean aberta = situacao == EstadoEleicao.EM_VOTACAO;
        boolean concluida = aberta && sessao.estaEncerrada();
        boolean podeDigitar = aberta && !concluida && !ocupado;
        Cargo cargoAtual = aberta && !concluida ? sessao.getCargoAtual() : null;
        boolean completo = cargoAtual != null
                && digitos.length() == cargoAtual.getQuantidadeDigitos();

        abrir.setEnabled(!ocupado && situacao == EstadoEleicao.AGUARDANDO_ABERTURA);
        encerrar.setEnabled(!ocupado && (situacao == EstadoEleicao.ENCERRAMENTO_PENDENTE
                || (aberta && (!sessao.estaIniciada() || controller.isVotosSalvos()))));
        relatorio.setEnabled(!ocupado && ultimoRelatorio != null);
        for (JButton tecla : teclas) {
            tecla.setEnabled(podeDigitar && !completo && !brancoSelecionado);
        }
        branco.setEnabled(podeDigitar && !brancoSelecionado);
        corrige.setEnabled(podeDigitar && (digitos.length() > 0 || brancoSelecionado));
        confirma.setEnabled(podeDigitar && (completo || brancoSelecionado));
        continuar.setVisible(concluida);
        continuar.setEnabled(concluida && !ocupado);
        continuar.setText(controller.isVotosSalvos() ? "Nova votação" : "Tentar novamente");
        foto.setIcon(null);
        nome.setText(" ");
        partido.setText(" ");
        numero.setText(" ");
        estado.setText(switch (situacao) {
            case AGUARDANDO_ABERTURA -> "Bloqueada";
            case EM_VOTACAO -> "Em votação";
            case ENCERRAMENTO_PENDENTE -> "Encerramento pendente";
            case ENCERRADA -> "Encerrada";
        });
        etapa.setText("Simulação educativa");
        if (!aberta) {
            cargo.setText(situacao == EstadoEleicao.AGUARDANDO_ABERTURA
                    ? "Aguardando abertura" : "Eleição encerrada");
            if (!ocupado) {
                orientacao.setText(situacao == EstadoEleicao.AGUARDANDO_ABERTURA
                        ? "Mesário: clique em Abrir eleição e informe a senha."
                        : situacao == EstadoEleicao.ENCERRAMENTO_PENDENTE
                        ? "Clique em Encerrar eleição para tentar concluir a leitura do relatório."
                        : "A votação está encerrada. Use Ver relatório para consultar a apuração.");
            }
        } else if (concluida) {
            boolean salvo = controller.isVotosSalvos();
            cargo.setText(salvo ? "FIM" : "Votação pendente");
            nome.setText(salvo ? "Votos salvos no Firestore." : "Envie novamente a votação.");
            if (!ocupado) {
                orientacao.setText(salvo ? "Clique em Nova votação para o próximo participante."
                        : "Mantenha a janela aberta e clique em Tentar novamente.");
            }
        } else {
            etapa.setText("Cargo " + (sessao.getIndiceCargo() + 1) + " de " + controller.getCargos().size());
            cargo.setText(cargoAtual.getNome());
            String visor = digitos + "_".repeat(cargoAtual.getQuantidadeDigitos() - digitos.length());
            numero.setText(brancoSelecionado ? " " : String.join(" ", visor.split("")));
            Candidato candidato = completo ? cargoAtual.buscarCandidato(digitos.toString()) : null;
            if (brancoSelecionado) {
                nome.setText("Voto em branco");
            } else if (candidato != null) {
                nome.setText(candidato.getNome());
                partido.setText(candidato.getPartido());
                foto.setIcon(retratos.get(candidato.getFoto()));
            } else if (completo) {
                nome.setText("Número sem candidato");
                partido.setText("CONFIRMA registra voto nulo.");
            }
            if (!ocupado) {
                orientacao.setText("Confira sua escolha. CORRIGE limpa o número, nome, partido e foto.");
            }
        }
        atualizarLista(cargoAtual);
        revalidate();
        repaint();
    }

    private void atualizarLista(Cargo cargoAtual) {
        candidatos.removeAll();
        if (cargoAtual != null) {
            candidatos.add(rotulo(cargoAtual.getNome(), 14, Font.PLAIN, SECUNDARIO));
            candidatos.add(Box.createVerticalStrut(15));
            for (Candidato candidato : cargoAtual.getCandidatos()) {
                candidatos.add(rotulo(candidato.getNumero(), 23, Font.BOLD, VERDE));
                candidatos.add(rotulo(candidato.getNome(), 15, Font.BOLD, TEXTO));
                candidatos.add(rotulo(candidato.getPartido(), 13, Font.PLAIN, SECUNDARIO));
                candidatos.add(Box.createVerticalStrut(17));
            }
        }
        candidatos.revalidate();
        candidatos.repaint();
    }

    protected String pedirSenha(String titulo) {
        JPasswordField senha = new JPasswordField(12);
        int opcao = JOptionPane.showConfirmDialog(this, senha, titulo + " · senha do mesário",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        char[] caracteres = senha.getPassword();
        String valor = opcao == JOptionPane.OK_OPTION ? new String(caracteres) : null;
        Arrays.fill(caracteres, '\0');
        senha.setText("");
        return valor;
    }

    private <T> void executar(Tarefa<T> tarefa, Consumer<T> sucesso, String mensagem) {
        if (ocupado) {
            return;
        }
        ocupado = true;
        orientacao.setText(mensagem);
        atualizarTela();
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarefa.executar();
            }

            @Override
            protected void done() {
                ocupado = false;
                try {
                    T resultado = get();
                    sucesso.accept(resultado);
                    atualizarTela();
                } catch (ExecutionException erro) {
                    atualizarTela();
                    mostrarErro(erro.getCause().getMessage());
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    atualizarTela();
                    mostrarErro("A operação foi interrompida.");
                }
            }
        }.execute();
    }

    protected void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Voto Consciente", JOptionPane.WARNING_MESSAGE);
    }

    protected void mostrarRelatorio(String titulo, RelatorioEleicao resultado, boolean modal) {
        ultimoRelatorio = resultado;
        tituloRelatorio = titulo;
        atualizarTela();
        TelaRelatorioEleicao.mostrar(this, titulo, resultado, modal);
    }

    public WindowAdapter criarControleFechamento(JFrame janela) {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                SessaoVotacao sessao = controller.getSessao();
                boolean pendente = sessao != null && sessao.estaIniciada() && !controller.isVotosSalvos();
                if ((ocupado || pendente) && JOptionPane.showConfirmDialog(TelaUrna.this,
                        "Há uma operação ou votação pendente. Deseja sair?",
                        "Fechar urna", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                janela.dispose();
            }
        };
    }

    private static JPanel coluna(Color cor) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(cor);
        return painel;
    }

    private static JLabel rotulo(String texto, int tamanho, int estilo, Color cor) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font(Font.SANS_SERIF, estilo, tamanho));
        label.setForeground(cor);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                label.getFontMetrics(label.getFont()).getHeight() + 3));
        return label;
    }

    private static JButton botao(String texto, Color fundo, Color cor) {
        JButton botao = new JButton(texto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        botao.setBackground(fundo);
        botao.setForeground(cor);
        botao.setOpaque(true);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fundo.darker()),
                BorderFactory.createEmptyBorder(10, 9, 10, 9)));
        return botao;
    }

    /** Mantém a entrada visao.TelaUrna disponível para os comandos antigos. */
    public static void main(String[] args) {
        aplicacao.Main.main(args);
    }

    @FunctionalInterface
    private interface Tarefa<T> {
        T executar() throws Exception;
    }
}
