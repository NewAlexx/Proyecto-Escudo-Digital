package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class Estilo {
    public static final Color FONDO = new Color(0x121218);
    public static final Color SUPERFICIE = new Color(0x1C1C26);
    public static final Color SUPERFICIE_ALTA = new Color(0x262633);
    public static final Color BORDE = new Color(0x33334A);
    public static final Color BORDE_FUERTE = new Color(0x4A4A66);
    public static final Color TEXTO = new Color(0xECECF2);
    public static final Color TEXTO_SUAVE = new Color(0xA3A3B8);

    public static final Color MAGENTA = new Color(0xC2187A);
    public static final Color MORADO = new Color(0x5E2A9B);
    public static final Color MAGENTA_OSCURO = new Color(0x8E1460);
    public static final Color MORADO_OSCURO = new Color(0x3B1A66);
    public static final Color MAGENTA_CLARO = new Color(0xF06BB3);
    public static final Color ACENTO = new Color(0xA64AC9);
    public static final Color ACENTO_OSCURO = new Color(0x5B2B7A);
    public static final Color ACENTO_CLARO = new Color(0xD9A6F2);

    public static final Color ROJO = new Color(0xFF5C6C);
    public static final Color ROJO_FUERTE = new Color(0xD93649);
    public static final Color VERDE = new Color(0x4CC38A);
    public static final Color NARANJA = new Color(0xFFB14E);
    public static final Color INFO = new Color(0x8FA8FF);

    public static final Color AZUL = ACENTO_CLARO;
    public static final Color AZUL_CLARO = SUPERFICIE_ALTA;

    public static final String FAMILIA = "Segoe UI";
    public static final Font NORMAL = new Font(FAMILIA, Font.PLAIN, 14);
    public static final Font NEGRITA = new Font(FAMILIA, Font.BOLD, 14);
    public static final Font TITULO = new Font(FAMILIA, Font.BOLD, 24);
    public static final Font SUBTITULO = new Font(FAMILIA, Font.BOLD, 16);
    public static final Font PEQUENA = new Font(FAMILIA, Font.PLAIN, 12);

    private Estilo() { }

    public static void aplicarTema() {
        try {
            MetalLookAndFeel.setCurrentTheme(new TemaOscuro());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignorado) {
        }
        String[] fuentes = {"Label.font", "Button.font", "TextField.font", "PasswordField.font", "TextArea.font",
            "ComboBox.font", "CheckBox.font", "RadioButton.font", "Table.font", "TableHeader.font",
            "TabbedPane.font", "OptionPane.messageFont", "OptionPane.buttonFont", "List.font", "ProgressBar.font"};
        for (String k : fuentes) UIManager.put(k, NORMAL);
        Object[][] colores = {
            {"Panel.background", FONDO}, {"OptionPane.background", SUPERFICIE}, {"OptionPane.messageForeground", TEXTO},
            {"Label.foreground", TEXTO}, {"CheckBox.background", SUPERFICIE}, {"CheckBox.foreground", TEXTO},
            {"RadioButton.background", SUPERFICIE}, {"RadioButton.foreground", TEXTO},
            {"TextField.background", SUPERFICIE_ALTA}, {"TextField.foreground", TEXTO}, {"TextField.caretForeground", TEXTO},
            {"TextField.inactiveBackground", SUPERFICIE}, {"TextField.inactiveForeground", TEXTO_SUAVE},
            {"PasswordField.background", SUPERFICIE_ALTA}, {"PasswordField.foreground", TEXTO},
            {"PasswordField.caretForeground", TEXTO}, {"TextArea.background", SUPERFICIE_ALTA},
            {"TextArea.foreground", TEXTO}, {"TextArea.caretForeground", TEXTO},
            {"ComboBox.background", SUPERFICIE_ALTA}, {"ComboBox.foreground", TEXTO},
            {"ComboBox.selectionBackground", ACENTO_OSCURO}, {"ComboBox.selectionForeground", TEXTO},
            {"List.background", SUPERFICIE_ALTA}, {"List.foreground", TEXTO},
            {"Table.background", SUPERFICIE}, {"Table.foreground", TEXTO}, {"Table.gridColor", BORDE},
            {"Table.selectionBackground", ACENTO_OSCURO}, {"Table.selectionForeground", TEXTO},
            {"Viewport.background", SUPERFICIE}, {"ScrollPane.background", SUPERFICIE},
            {"ProgressBar.background", SUPERFICIE_ALTA}, {"ProgressBar.foreground", ACENTO},
            {"ProgressBar.selectionForeground", TEXTO}, {"ProgressBar.selectionBackground", TEXTO},
            {"Button.background", SUPERFICIE_ALTA}, {"Button.foreground", TEXTO}, {"Button.select", ACENTO_OSCURO},
            {"TitledBorder.titleColor", ACENTO_CLARO}, {"ToolTip.background", SUPERFICIE_ALTA}, {"ToolTip.foreground", TEXTO},
            {"SplitPane.background", FONDO}, {"SplitPaneDivider.draggingColor", BORDE},
        };
        for (Object[] par : colores) UIManager.put(par[0], par[1]);
        UIManager.put("ProgressBar.border", BorderFactory.createLineBorder(BORDE));
        UIManager.put("TextField.border", campoBorde());
        UIManager.put("PasswordField.border", campoBorde());
        UIManager.put("ComboBox.border", BorderFactory.createLineBorder(BORDE));
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPaneUI", PestanasUI.class.getName());
        UIManager.put("ScrollBarUI", DesplazamientoUI.class.getName());
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(TITULO);
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(SUBTITULO);
        l.setForeground(ACENTO_CLARO);
        return l;
    }

    public static JLabel ayuda(String texto) {
        JLabel l = new JLabel("<html>" + texto + "</html>");
        l.setFont(NORMAL);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JLabel ayudaAncho(String texto, int ancho) {
        JLabel l = new JLabel("<html><div style='width:" + ancho + "px'>" + texto + "</div></html>");
        l.setFont(NORMAL);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JTextArea parrafo(String texto) {
        JTextArea t = new JTextArea(texto);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setFocusable(false);
        t.setOpaque(false);
        t.setFont(NORMAL);
        t.setForeground(TEXTO_SUAVE);
        t.setBorder(BorderFactory.createEmptyBorder());
        return t;
    }

    public static JPanel encabezado(String titulo, String descripcion) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(titulo(titulo), BorderLayout.NORTH);
        p.add(parrafo(descripcion), BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        return p;
    }

    public static Border campoBorde() {
        return BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDE_FUERTE),
                BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    public static <T extends JTextField> T campo(T c) {
        c.setBackground(SUPERFICIE_ALTA);
        c.setForeground(TEXTO);
        c.setCaretColor(TEXTO);
        c.setBorder(campoBorde());
        return c;
    }

    public static JPanel conIcono(Icono.Tipo tipo, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel ic = new JLabel(new Icono(tipo, 22, ACENTO_CLARO));
        ic.setVerticalAlignment(SwingConstants.CENTER);
        p.add(ic, BorderLayout.WEST);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    public static JButton boton(String texto, boolean primario) {
        return new BotonRedondo(texto, primario ? BotonRedondo.Tipo.PRINCIPAL : BotonRedondo.Tipo.SECUNDARIO);
    }

    public static JButton botonPeligro(String texto) {
        return new BotonRedondo(texto, BotonRedondo.Tipo.PELIGRO);
    }

    public static PanelRedondeado tarjeta(java.awt.LayoutManager layout) {
        return new PanelRedondeado(layout);
    }

    public static Border bordeTarjeta() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16));
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        s.getViewport().setBackground(SUPERFICIE);
        s.getVerticalScrollBar().setUnitIncrement(16);
        s.getVerticalScrollBar().setPreferredSize(new Dimension(12, 12));
        s.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 12));
        return s;
    }

    public static JPanel contenedor() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));
        return p;
    }

    public static JTabbedPane pestanas() {
        JTabbedPane t = new JTabbedPane();
        t.setBackground(FONDO);
        return t;
    }

    public static JEditorPane areaHtml(String html) {
        JEditorPane ep = new JEditorPane("text/html", envolver(html));
        ep.setEditable(false);
        ep.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        ep.setFont(NORMAL);
        ep.setForeground(TEXTO);
        ep.setBackground(SUPERFICIE);
        ep.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return ep;
    }

    public static void ponerHtml(JEditorPane ep, String html) {
        ep.setText(envolver(html));
        ep.setCaretPosition(0);
    }

    private static String envolver(String cuerpo) {
        return "<html><body style='font-family:" + FAMILIA + ",sans-serif;font-size:13pt;color:" + hex(TEXTO) + "'>"
                + cuerpo + "</body></html>";
    }

    public static void mostrarHtml(Component padre, String titulo, String html) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(padre), titulo, JDialog.ModalityType.APPLICATION_MODAL);
        JEditorPane ep = areaHtml(html);
        ep.setCaretPosition(0);
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        p.add(scroll(ep), BorderLayout.CENTER);
        JButton cerrar = boton("Cerrar", true);
        cerrar.addActionListener(e -> d.dispose());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        sur.add(cerrar);
        p.add(sur, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.setSize(780, 580);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }

    public static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    public static String esc(String t) {
        if (t == null) return "";
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static String lista(List<String> items) {
        StringBuilder sb = new StringBuilder("<ul style='margin-top:4px'>");
        for (String i : items) sb.append("<li style='margin-bottom:5px'>").append(esc(i)).append("</li>");
        return sb.append("</ul>").toString();
    }

    public static String h(String texto) {
        return "<h3 style='color:" + hex(ACENTO_CLARO) + ";font-size:15pt;margin-bottom:4px;margin-top:14px'>" + esc(texto) + "</h3>";
    }

    public static String suave(String htmlYaEscapado) {
        return "<p style='color:" + hex(TEXTO_SUAVE) + "'>" + htmlYaEscapado + "</p>";
    }

    public static String aviso(String texto, Color color) {
        return "<table width='100%' cellpadding='9' style='border:1px solid " + hex(color) + ";background:" + fondoDe(color)
                + "'><tr><td style='color:" + hex(color) + ";font-weight:bold'>" + texto + "</td></tr></table>";
    }

    private static String fondoDe(Color c) {
        if (c.equals(ROJO)) return "#3A1F28";
        if (c.equals(VERDE)) return "#1C3329";
        if (c.equals(NARANJA)) return "#3A2E1C";
        return "#2A2140";
    }

    public static Color colorNivel(String clasificacion) {
        String c = clasificacion == null ? "" : clasificacion.toLowerCase();
        if (c.contains("crític") || c.contains("muy débil") || c.equals("débil") || c.contains("comprometida")
                || c.contains("phishing probable") || c.contains("alta") || c.equals("alto") || c.contains("vulnerable")) {
            return ROJO;
        }
        if (c.contains("aceptable") || c.contains("sospechoso") || c.contains("medio") || c.contains("moderad")
                || c.contains("en formación")) {
            return NARANJA;
        }
        return VERDE;
    }

    public static String barra(double valor, Color color) {
        int ancho = (int) Math.max(1, Math.min(100, Math.round(valor)));
        return "<table cellpadding='0' cellspacing='0' width='220' style='border:1px solid " + hex(BORDE) + "'><tr>"
                + "<td width='" + (ancho * 2.2) + "' bgcolor='" + hex(color) + "' height='12'></td>"
                + (ancho < 100 ? "<td bgcolor='" + hex(SUPERFICIE_ALTA) + "'></td>" : "") + "</tr></table>";
    }

    public static JTable tabla(String... columnas) {
        DefaultTableModel m = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(30);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setBackground(SUPERFICIE);
        t.setForeground(TEXTO);
        t.setGridColor(BORDE);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(ACENTO_OSCURO);
        t.setSelectionForeground(TEXTO);
        DefaultTableCellRenderer cab = new DefaultTableCellRenderer();
        cab.setBackground(SUPERFICIE_ALTA);
        cab.setForeground(ACENTO_CLARO);
        cab.setFont(NEGRITA);
        cab.setHorizontalAlignment(SwingConstants.LEFT);
        cab.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        t.getTableHeader().setDefaultRenderer(cab);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBackground(SUPERFICIE_ALTA);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foco, int f, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, false, f, col);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!sel) c.setBackground(f % 2 == 0 ? SUPERFICIE : new Color(0x20202B));
                c.setForeground(TEXTO);
                return c;
            }
        });
        return t;
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Escudo Digital", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Escudo Digital", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Escudo Digital", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void tamanoMinimo(JComponent c, int ancho, int alto) {
        c.setPreferredSize(new Dimension(ancho, alto));
    }
}
