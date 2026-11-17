package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.Estudiante;
import modelo.Evaluacion;
import modelo.EvaluacionGlobal;
import modelo.SesionEntrenamiento;
import negocio.GestorEntrenamiento;
import negocio.GestorRiesgoGlobal;
import negocio.ValidacionException;

public class PanelRiesgo extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorRiesgoGlobal gestor;
    private final JEditorPane resultado = Estilo.areaHtml("");
    private final JTable tabla = Estilo.tabla("Código", "Fecha", "IGRD", "Nivel", "Tendencia");
    private final GestorEntrenamiento gestorEntrenamiento;
    private final JEditorPane resiliencia = Estilo.areaHtml("");
    private final JTable tablaCuestionarios = Estilo.tabla("Código", "Fecha", "Aciertos", "Puntaje", "Nivel");

    public PanelRiesgo(ContextoAplicacion ctx, Estudiante est) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorRiesgoGlobal();
        this.gestorEntrenamiento = ctx.getGestorEntrenamiento();
        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Índice global de riesgo digital",
                "Combina los resultados de los tres módulos en un solo índice y te propone un plan con 3 acciones. "
                + "Aquí también ves tu Índice de Resiliencia ante el phishing."), BorderLayout.NORTH);
        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        b.setOpaque(false);
        JButton calcular = Estilo.boton("Calcular mi índice", true);
        calcular.addActionListener(e -> calcular());
        b.add(calcular);
        arriba.add(b, BorderLayout.NORTH);
        arriba.add(Estilo.scroll(resultado), BorderLayout.CENTER);
        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.setOpaque(false);
        abajo.add(Estilo.subtitulo("Historial de evaluaciones"), BorderLayout.NORTH);
        abajo.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        abajo.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        split.setResizeWeight(0.7);
        split.setBorder(null);
        split.setOpaque(false);
        JPanel global = new JPanel(new BorderLayout());
        global.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        global.add(split, BorderLayout.CENTER);
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        tabs.addTab("Índice global de riesgo", global);
        tabs.addTab("Índice de Resiliencia", panelResiliencia());
        c.add(tabs, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    private JPanel panelResiliencia() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        javax.swing.JScrollPane sr = Estilo.scroll(resiliencia);
        sr.setPreferredSize(new java.awt.Dimension(400, 170));
        p.add(sr, BorderLayout.NORTH);
        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.add(Estilo.subtitulo("Historial de cuestionarios"), BorderLayout.NORTH);
        abajo.add(Estilo.scroll(tablaCuestionarios), BorderLayout.CENTER);
        p.add(abajo, BorderLayout.CENTER);
        return p;
    }

    private void actualizarResiliencia() {
        DefaultTableModel m = (DefaultTableModel) tablaCuestionarios.getModel();
        m.setRowCount(0);
        for (SesionEntrenamiento s : gestorEntrenamiento.listar(est)) {
            m.addRow(new Object[] {s.getId(), s.getFecha().format(Evaluacion.FORMATO_FECHA), s.getAciertos() + " de 10",
                String.format("%.2f", s.getPuntaje()), s.getClasificacion()});
        }
        if (gestorEntrenamiento.listar(est).isEmpty()) {
            Estilo.ponerHtml(resiliencia, "<p>Aún no completas ningún cuestionario. Hazlo en «Entrenamiento antiphishing».</p>"
                    + "<p style='color:#A3A3B8'>El Índice de Resiliencia usa tus tres últimos cuestionarios y es el 20 % del índice global.</p>");
        } else {
            double ir = gestorEntrenamiento.calcularResiliencia(est);
            String nivel = GestorEntrenamiento.nivelResiliencia(ir);
            Estilo.ponerHtml(resiliencia, Estilo.aviso(String.format("Índice de Resiliencia: %.2f / 100 &nbsp;·&nbsp; %s", ir,
                    Estilo.esc(nivel)), Estilo.colorNivel(nivel))
                    + "<p>IR = 0,5 × último + 0,3 × penúltimo + 0,2 × antepenúltimo cuestionario. "
                    + "En el índice global, el riesgo de resiliencia es 100 − IR (peso 0,20).</p>"
                    + String.format("<p style='color:#A3A3B8'>Cuestionarios hoy: %d de %d.</p>",
                    gestorEntrenamiento.sesionesDeHoy(est), GestorEntrenamiento.MAX_SESIONES_DIARIAS));
        }
    }

    private void calcular() {
        try {
            EvaluacionGlobal g = gestor.calcularIndice(est);
            ctx.guardar();
            Estilo.ponerHtml(resultado, detalle(g));
            actualizarTabla();
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private String detalle(EvaluacionGlobal g) {
        StringBuilder sb = new StringBuilder();
        sb.append(Estilo.aviso(String.format("Índice Global de Riesgo Digital: %.2f / 100 &nbsp;·&nbsp; Riesgo %s", g.getPuntaje(),
                Estilo.esc(g.getClasificacion())), Estilo.colorNivel(g.getClasificacion())));
        sb.append("<p>Tendencia: <b>").append(Estilo.esc(g.getTendencia())).append("</b></p>");
        if (g.getMotivoEscalamiento() != null) {
            sb.append(Estilo.aviso("Riesgo elevado a Crítico: " + Estilo.esc(g.getMotivoEscalamiento()) + ".", Estilo.ROJO));
        }
        sb.append(Estilo.h("Riesgo por componente")).append("<table cellpadding='4'>");
        double[] r = g.getRiesgos();
        for (int i = 0; i < r.length; i++) {
            sb.append("<tr><td>").append(EvaluacionGlobal.COMPONENTES[i]).append("</td><td>")
              .append(Estilo.barra(r[i], r[i] >= 60 ? Estilo.ROJO : r[i] >= 30 ? Estilo.NARANJA : Estilo.VERDE))
              .append("</td><td><b>").append(String.format("%.1f", r[i])).append("</b></td><td style='color:#A3A3B8'>peso ")
              .append(String.format("%.2f", EvaluacionGlobal.PESOS[i])).append("</td></tr>");
        }
        sb.append("</table>");
        for (String falta : g.getDatosFaltantes()) {
            sb.append("<p style='color:#FFB14E'>Datos incompletos: aún no registras ").append(Estilo.esc(falta))
              .append("; se usó un valor de 50.</p>");
        }
        sb.append(Estilo.h("Plan de acción")).append(Estilo.lista(g.getRecomendaciones()));
        return sb.toString();
    }

    private void actualizarTabla() {
        DefaultTableModel m = (DefaultTableModel) tabla.getModel();
        m.setRowCount(0);
        for (EvaluacionGlobal g : gestor.listar(est)) {
            m.addRow(new Object[] {g.getId(), g.getFecha().format(Evaluacion.FORMATO_FECHA), String.format("%.2f", g.getPuntaje()),
                g.getClasificacion(), g.getTendencia()});
        }
    }

    @Override
    public void actualizar() {
        actualizarTabla();
        actualizarResiliencia();
        EvaluacionGlobal ultima = est.getUltimaEvaluacion(EvaluacionGlobal.class);
        Estilo.ponerHtml(resultado, ultima == null
                ? "<p>Aún no calculas tu índice. Pulsa «Calcular mi índice». Si te falta algún módulo, se usará un valor neutro de 50.</p>"
                : "<p style='color:#A3A3B8'>Última evaluación guardada:</p>" + detalle(ultima));
    }
}
