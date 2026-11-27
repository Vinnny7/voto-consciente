package visao;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.Cargo;
import modelo.LinhaApuracao;
import modelo.RelatorioEleicao;

/** Apresenta zerésima e apuração em abas, uma para cada cargo. */
public final class TelaRelatorioEleicao extends JPanel {
    public TelaRelatorioEleicao(String titulo, RelatorioEleicao relatorio) {
        super(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel cabecalho = new JLabel(titulo);
        cabecalho.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        add(cabecalho, BorderLayout.NORTH);
        JTabbedPane abas = new JTabbedPane();
        for (Cargo cargo : relatorio.getCargos()) {
            DefaultTableModel modelo = new DefaultTableModel(new Object[] {"Resultado"}, 0) {
                @Override
                public boolean isCellEditable(int linha, int coluna) {
                    return false;
                }
            };
            for (LinhaApuracao linha : relatorio.getLinhas(cargo)) {
                modelo.addRow(new Object[] {linha.getTexto()});
            }
            JTable tabela = new JTable(modelo);
            tabela.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
            tabela.setRowHeight(42);
            tabela.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            abas.addTab(cargo.getNome(), new JScrollPane(tabela));
        }
        add(abas, BorderLayout.CENTER);
        JLabel total = new JLabel("Participantes com votação salva: "
                + relatorio.getTotalParticipantes() + "  ·  Eleição " + relatorio.getEleicaoId());
        total.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        add(total, BorderLayout.SOUTH);
    }

    public static void mostrar(java.awt.Component pai, String titulo, RelatorioEleicao relatorio,
                               boolean modal) {
        var dono = javax.swing.SwingUtilities.getWindowAncestor(pai);
        JDialog janela = new JDialog(dono, titulo,
                modal ? java.awt.Dialog.ModalityType.APPLICATION_MODAL
                        : java.awt.Dialog.ModalityType.MODELESS);
        janela.setContentPane(new TelaRelatorioEleicao(titulo, relatorio));
        janela.setSize(850, 410);
        janela.setMinimumSize(new java.awt.Dimension(720, 350));
        janela.setLocationRelativeTo(pai);
        janela.setVisible(true);
    }
}
