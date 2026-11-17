package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.DiagnosticoHigiene;
import modelo.Estudiante;
import negocio.GestorDiagnostico;
import negocio.ValidacionException;

public class PanelDiagnostico extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorDiagnostico gestor;
    private final ButtonGroup[] grupos = new ButtonGroup[GestorDiagnostico.PREGUNTAS.length];
    private final JRadioButton[][] opciones = new JRadioButton[GestorDiagnostico.PREGUNTAS.length][5];
    private final List<JCheckBox> sitios = new ArrayList<>();
    private final JPanel panelSitios = new JPanel(new GridLayout(0, 3, 6, 2));
    private final JTable tabla = Estilo.tabla("Código", "Fecha", "IHD", "Clasificación");

    public PanelDiagnostico(ContextoAplicacion ctx, Estudiante est) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorDiagnostico();
        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Diagnóstico de higiene digital",
                "Responde 10 preguntas sobre tus hábitos. Escala: 1 = Nunca, 2 = Casi nunca, 3 = A veces, 4 = Casi siempre, 5 = Siempre."),
                BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Realizar diagnóstico", cuestionario());
        tabs.addTab("Historial", historial());
        c.add(tabs, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    private JPanel cuestionario() {
        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Estilo.SUPERFICIE);
        lista.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        String dimension = "";
        for (int i = 0; i < GestorDiagnostico.PREGUNTAS.length; i++) {
            String dim = DiagnosticoHigiene.DIMENSIONES[i / 2];
            if (!dim.equals(dimension)) {
                dimension = dim;
                JLabel d = Estilo.subtitulo(dim);
                d.setBorder(BorderFactory.createEmptyBorder(i == 0 ? 0 : 14, 0, 4, 0));
                d.setAlignmentX(LEFT_ALIGNMENT);
                lista.add(d);
            }
            JLabel p = new JLabel("<html><b>P" + (i + 1) + ".</b> " + Estilo.esc(GestorDiagnostico.PREGUNTAS[i]) + "</html>");
            p.setAlignmentX(LEFT_ALIGNMENT);
            p.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
            lista.add(p);
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            fila.setOpaque(false);
            fila.setAlignmentX(LEFT_ALIGNMENT);
            grupos[i] = new ButtonGroup();
            for (int v = 0; v < 5; v++) {
                JRadioButton r = new JRadioButton((v + 1) + " " + GestorDiagnostico.ESCALA[v]);
                r.setOpaque(false);
                opciones[i][v] = r;
                grupos[i].add(r);
                fila.add(r);
                if (i == 0) {
                    final int valor = v + 1;
                    r.addActionListener(e -> panelSitios.setVisible(GestorDiagnostico.debePreguntarReutilizacion(valor)));
                }
            }
            lista.add(fila);
            if (i == 0) {
                panelSitios.setOpaque(false);
                panelSitios.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("¿En qué tipos de páginas repites la misma contraseña?"),
                        BorderFactory.createEmptyBorder(2, 6, 4, 6)));
                for (String s : GestorDiagnostico.TIPOS_SITIO) {
                    JCheckBox cb = new JCheckBox(s);
                    cb.setOpaque(false);
                    sitios.add(cb);
                    panelSitios.add(cb);
                }
                panelSitios.setVisible(false);
                panelSitios.setAlignmentX(LEFT_ALIGNMENT);
                lista.add(panelSitios);
            }
        }
        lista.add(Box.createVerticalStrut(10));

        JPanel p = new JPanel(new BorderLayout());
        p.add(Estilo.scroll(lista), BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        JButton limpiar = Estilo.boton("Borrar respuestas", false);
        JButton calcular = Estilo.boton("Calcular mi índice", true);
        limpiar.addActionListener(e -> limpiarRespuestas());
        calcular.addActionListener(e -> calcular());
        sur.add(limpiar);
        sur.add(calcular);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void limpiarRespuestas() {
        for (ButtonGroup g : grupos) g.clearSelection();
        for (JCheckBox cb : sitios) cb.setSelected(false);
        panelSitios.setVisible(false);
    }

    private void calcular() {
        int[] r = new int[GestorDiagnostico.PREGUNTAS.length];
        for (int i = 0; i < r.length; i++) {
            for (int v = 0; v < 5; v++) if (opciones[i][v].isSelected()) r[i] = v + 1;
            if (r[i] == 0) {
                Estilo.error(this, "Debes responder las 10 preguntas antes de continuar. Falta la pregunta " + (i + 1) + ".");
                return;
            }
        }
        List<String> elegidos = new ArrayList<>();
        for (JCheckBox cb : sitios) if (cb.isSelected()) elegidos.add(cb.getText());
        try {
            DiagnosticoHigiene d = gestor.realizarDiagnostico(est, r, elegidos);
            ctx.guardar();
            Estilo.mostrarHtml(this, "Resultado del diagnóstico", detalle(d, gestor.compararConAnterior(est, d)));
            limpiarRespuestas();
            actualizar();
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private String detalle(DiagnosticoHigiene d, String comparacion) {
        Color color = Estilo.colorNivel(d.getClasificacion());
        StringBuilder sb = new StringBuilder();
        sb.append(Estilo.aviso(String.format("Índice de Higiene Digital: %.2f / 100 &nbsp;·&nbsp; %s", d.getIHD(),
                Estilo.esc(d.getClasificacion())), color));
        if (comparacion != null) sb.append("<p>").append(Estilo.esc(comparacion)).append("</p>");
        sb.append(Estilo.h("Puntaje por dimensión"));
        sb.append("<table cellpadding='4'>");
        double[] pd = d.getPuntajesDimension();
        for (int i = 0; i < pd.length; i++) {
            sb.append("<tr><td>").append(DiagnosticoHigiene.DIMENSIONES[i]).append("</td><td>")
              .append(Estilo.barra(pd[i], pd[i] < 50 ? Estilo.ROJO : pd[i] < 75 ? Estilo.NARANJA : Estilo.VERDE))
              .append("</td><td><b>").append(String.format("%.1f", pd[i])).append("</b></td><td style='color:#A3A3B8'>peso ")
              .append(String.format("%.2f", DiagnosticoHigiene.PESOS[i])).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append(Estilo.h("Recomendaciones")).append(Estilo.lista(d.getRecomendaciones()));
        return sb.toString();
    }

    private JPanel historial() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton ver = Estilo.boton("Ver detalle", true);
        JButton eliminar = Estilo.boton("Eliminar", false);
        ver.addActionListener(e -> {
            DiagnosticoHigiene d = seleccionado();
            if (d != null) Estilo.mostrarHtml(this, "Diagnóstico " + d.getId(), detalle(d, null));
        });
        eliminar.addActionListener(e -> {
            DiagnosticoHigiene d = seleccionado();
            if (d == null || !Estilo.confirmar(this, "¿Eliminar el diagnóstico " + d.getId() + "?")) return;
            try {
                gestor.eliminar(est, d.getId());
                ctx.guardar();
                actualizar();
            } catch (ValidacionException ex) {
                Estilo.error(this, ex.getMessage());
            }
        });
        sur.add(ver);
        sur.add(eliminar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private DiagnosticoHigiene seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.info(this, "Selecciona un diagnóstico de la tabla.");
            return null;
        }
        try {
            return gestor.buscar(est, (String) tabla.getValueAt(fila, 0));
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
            return null;
        }
    }

    @Override
    public void actualizar() {
        DefaultTableModel m = (DefaultTableModel) tabla.getModel();
        m.setRowCount(0);
        for (DiagnosticoHigiene d : gestor.listar(est)) {
            m.addRow(new Object[] {d.getId(), d.getFecha().format(modelo.Evaluacion.FORMATO_FECHA),
                String.format("%.2f", d.getIHD()), d.getClasificacion()});
        }
    }
}
