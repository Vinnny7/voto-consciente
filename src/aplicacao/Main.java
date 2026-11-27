package aplicacao;

import configuracao.ConfiguracaoEnv;
import controle.EleicaoController;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import modelo.Candidato;
import modelo.Cargo;
import modelo.CargoMajoritario;
import modelo.CargoProporcional;
import persistencia.AutenticacaoGoogleCloud;
import persistencia.FirestoreProxy;
import visao.TelaUrna;

/** Monta o catálogo fictício e liga o controlador ao proxy do Firestore. */
public final class Main {
    private Main() {}

    public static List<Cargo> criarCargos() {
        return List.of(
                new CargoProporcional("deputadoFederal", "Deputado federal", 4, List.of(
                        new Candidato("9101", "Ana do Ipê", "Partido do Ipê", "/fotos/ana-ipe.png"),
                        new Candidato("9202", "Lucas do Sol", "Partido do Sol", "/fotos/lucas-sol.png"))),
                new CargoMajoritario("presidente", "Presidente", 2, List.of(
                        new Candidato("91", "Marina do Ipê", "Partido do Ipê", "/fotos/marina-ipe.png"),
                        new Candidato("92", "Rafael do Sol", "Partido do Sol", "/fotos/rafael-sol.png"))));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Path arquivo = localizarConfiguracao();
                ConfiguracaoEnv config = new ConfiguracaoEnv(arquivo);
                var repositorio = new FirestoreProxy(
                        config.obter("FIREBASE_PROJECT_ID", "voto-consciente-b1b46"),
                        config.obter("FIREBASE_DATABASE_ID", "(default)"),
                        new AutenticacaoGoogleCloud());
                var controller = new EleicaoController(criarCargos(), repositorio);
                TelaUrna tela = new TelaUrna(controller);
                JFrame janela = new JFrame("Voto Consciente");
                janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                janela.addWindowListener(tela.criarControleFechamento(janela));
                janela.setContentPane(tela);
                janela.setSize(1080, 690);
                janela.setMinimumSize(new java.awt.Dimension(980, 620));
                janela.setLocationRelativeTo(null);
                janela.setVisible(true);
            } catch (Exception erro) {
                JOptionPane.showMessageDialog(null, erro.getMessage(),
                        "Configuração da urna", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static Path localizarConfiguracao() throws URISyntaxException {
        Path atual = Path.of(".env").toAbsolutePath();
        if (Files.isRegularFile(atual)) {
            return atual;
        }
        Path origem = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return origem.getParent().resolve(".env");
    }
}
