package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import modelo.ConsultaCorreo;
import modelo.Estudiante;
import modelo.EvaluacionContrasena;
import negocio.GestorCredenciales;
import negocio.ServicioNoDisponibleException;
import negocio.ValidacionException;

public class PanelCredenciales extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorCredenciales gestor;

    private final JPasswordField clave = new JPasswordField(24);
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel veredicto = new JLabel(" ");
    private final JEditorPane resultadoClave = Estilo.areaHtml("");
    private final JButton porQue = Estilo.boton("¿Por qué obtuve este puntaje?", false);
    private EvaluacionContrasena ultima;

    private final JTextField correo = new JTextField(28);
    private final JCheckBox consentimiento = new JCheckBox(
            "Autorizo enviar este correo a XposedOrNot (o a LeakCheck si el primero no responde).");
    private final JEditorPane resultadoCorreo = Estilo.areaHtml("");

    public PanelCredenciales(ContextoAplicacion ctx, Estudiante est) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorCredenciales();
        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Contraseñas y correos filtrados",
                "Evalúa qué tan segura es una contraseña y comprueba si tu correo aparece en filtraciones de datos."), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Evaluar una contraseña", pestanaClave());
        tabs.addTab("Verificar mi correo", pestanaCorreo());
        c.add(tabs, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    private JPanel pestanaClave() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        JLabel l = new JLabel("Contraseña:");
        l.setFont(Estilo.NEGRITA);
        clave.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.BORDE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JCheckBox mostrar = new JCheckBox("Mostrar");
        mostrar.setOpaque(false);
        char eco = clave.getEchoChar();
        mostrar.addActionListener(e -> clave.setEchoChar(mostrar.isSelected() ? (char) 0 : eco));
        JButton evaluar = Estilo.boton("Evaluar", true);
        evaluar.addActionListener(e -> evaluarContrasena());
        clave.addActionListener(e -> evaluarContrasena());
        fila.add(l);
        fila.add(clave);
        fila.add(mostrar);
        fila.add(evaluar);

        barra.setStringPainted(true);
        barra.setString("Sin evaluar");
        barra.setPreferredSize(new java.awt.Dimension(500, 26));
        veredicto.setFont(Estilo.SUBTITULO);

        JPanel form = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        form.setBackground(Estilo.SUPERFICIE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        form.add(Estilo.parrafo("La contraseña no se guarda. Primero se evalúa en tu equipo, sin Internet; "
                + "después decides si quieres verificar si fue filtrada."));
        form.add(fila);
        form.add(barra);
        form.add(veredicto);

        JPanel p = new JPanel(new BorderLayout());
        p.add(form, BorderLayout.NORTH);
        p.add(Estilo.scroll(resultadoClave), BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        porQue.setEnabled(false);
        porQue.addActionListener(e -> {
            if (ultima != null) Estilo.mostrarHtml(this, "¿Por qué obtuve este puntaje?", explicacion(ultima));
        });
        sur.add(porQue);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void evaluarContrasena() {
        char[] c = clave.getPassword();
        clave.setText("");
        EvaluacionContrasena ev;
        try {
            ev = gestor.evaluarFortaleza(est, c);
        } catch (ValidacionException ex) {
            Arrays.fill(c, '\0');
            Estilo.error(this, ex.getMessage());
            return;
        }
        ultima = ev;
        int pts = (int) Math.round(ev.getPuntaje());
        Color color = pts >= 60 ? Estilo.VERDE : pts >= 40 ? Estilo.NARANJA : Estilo.ROJO;
        barra.setValue(pts);
        barra.setForeground(color);
        barra.setString(pts + " / 100 · " + ev.getNivelFortaleza());
        veredicto.setForeground(pts >= 60 ? Estilo.VERDE : Estilo.ROJO);
        veredicto.setText(pts >= 60 ? "Por su estructura, tu contraseña es SEGURA." : "Por su estructura, tu contraseña NO es segura.");
        porQue.setEnabled(true);
        Estilo.ponerHtml(resultadoClave, "<p style='color:#A3A3B8'>Esperando tu decisión sobre la verificación de filtraciones...</p>");

        String mensaje = "<html><body style='width:420px'><b>¿Quieres verificar si esta contraseña fue filtrada?</b><br><br>"
                + "Una contraseña fuerte puede no servir si ya apareció en una filtración de datos. Escudo Digital puede "
                + "comprobarlo con <b>Pwned Passwords</b> (Have I Been Pwned):<ul>"
                + "<li>Tu contraseña <b>NO</b> se envía: solo los 5 primeros caracteres de su hash SHA-1.</li>"
                + "<li>El servicio responde con una lista de hashes parecidos y la comparación se hace en tu equipo.</li></ul></body></html>";
        Object[] botones = {"Sí, verificar mi contraseña", "No, continuar sin verificar"};
        int r = JOptionPane.showOptionDialog(this, mensaje, "Verificar filtraciones", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, botones, botones[0]);
        boolean autoriza = r == 0;
        if (!autoriza) {
            gestor.verificarFiltracion(ev, c, false);
            ctx.guardar();
            mostrarResultadoClave(ev);
            return;
        }
        Estilo.ponerHtml(resultadoClave, "<p>Consultando Pwned Passwords...</p>");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                gestor.verificarFiltracion(ev, c, true);
                return null;
            }

            @Override
            protected void done() {
                Arrays.fill(c, '\0');
                ctx.guardar();
                mostrarResultadoClave(ev);
            }
        }.execute();
    }

    private void mostrarResultadoClave(EvaluacionContrasena ev) {
        StringBuilder sb = new StringBuilder();
        if (!ev.fueAutorizada()) {
            sb.append(Estilo.aviso("No se consultó Pwned Passwords. Se muestra solo el puntaje de fortaleza.", Estilo.AZUL));
        } else if (!ev.fueConsultada()) {
            sb.append(Estilo.aviso("No se pudo conectar con Pwned Passwords. Se muestra solo el puntaje de fortaleza.", Estilo.NARANJA));
        } else if (ev.estaComprometida()) {
            sb.append(Estilo.aviso("Alerta: esta contraseña apareció " + String.format("%,d", ev.getVecesFiltrada())
                    + " veces en filtraciones. No la uses.", Estilo.ROJO));
        } else {
            sb.append(Estilo.aviso("Esta contraseña no aparece en filtraciones conocidas.", Estilo.VERDE));
        }
        sb.append(String.format("<p>Puntos a favor: <b>%d</b> &nbsp;·&nbsp; Puntos en contra: <b>%d</b> &nbsp;·&nbsp; "
                + "Puntaje = %d − %d</p>", ev.getTotalSumas(), ev.getTotalRestas(), ev.getTotalSumas(), ev.getTotalRestas()));
        sb.append(Estilo.h("Recomendaciones")).append(Estilo.lista(ev.getRecomendaciones()));
        if (ev.necesitaGuia()) {
            sb.append(Estilo.h("Guía: cómo crear una contraseña segura"))
              .append(Estilo.lista(Arrays.asList(EvaluacionContrasena.GUIA_CONTRASENA_SEGURA)));
        }
        sb.append("<p style='color:#A3A3B8'>Usa el botón «¿Por qué obtuve este puntaje?» para ver cada suma y resta explicada.</p>");
        Estilo.ponerHtml(resultadoClave, sb.toString());
    }

    private String explicacion(EvaluacionContrasena ev) {
        StringBuilder sb = new StringBuilder();
        sb.append(Estilo.aviso(String.format("Puntaje: %d − %d = %.0f puntos (%s)", ev.getTotalSumas(), ev.getTotalRestas(),
                ev.getPuntaje(), ev.getNivelFortaleza()), Estilo.colorNivel(ev.getNivelFortaleza())));
        sb.append("<p>El puntaje empieza en 0. <b>Suman</b> las características que hacen difícil adivinar la contraseña y "
                + "<b>restan</b> los patrones que la hacen predecible. El resultado queda entre 0 y 100.</p>"
                + "<p style='color:#A3A3B8'>Niveles: 0-19 Muy débil · 20-39 Débil · 40-59 Aceptable · 60-79 Fuerte · 80-100 Muy fuerte.</p>");
        List<String> det = ev.getDetalle();
        List<String> exp = ev.getExplicaciones();
        sb.append(Estilo.h("Lo que suma (" + ev.getTotalSumas() + " puntos)")).append("<table cellpadding='5'>");
        for (int i = 0; i < det.size(); i++) {
            if (det.get(i).startsWith("+")) fila(sb, det.get(i), exp.get(i), "#4CC38A");
        }
        sb.append("</table>").append(Estilo.h("Lo que resta (" + ev.getTotalRestas() + " puntos)")).append("<table cellpadding='5'>");
        boolean hay = false;
        for (int i = 0; i < det.size(); i++) {
            if (det.get(i).startsWith("-")) {
                fila(sb, det.get(i), exp.get(i), "#FF5C6C");
                hay = true;
            }
        }
        if (!hay) sb.append("<tr><td>No se encontraron patrones predecibles.</td></tr>");
        return sb.append("</table>").toString();
    }

    private static void fila(StringBuilder sb, String linea, String explicacion, String color) {
        int dos = linea.lastIndexOf(':');
        String concepto = linea.substring(2, dos);
        String valor = linea.substring(dos + 1).trim();
        sb.append("<tr><td valign='top' width='60' style='color:").append(color).append(";font-weight:bold'>")
          .append(Estilo.esc(valor)).append("</td><td><b>").append(Estilo.esc(concepto)).append("</b><br>")
          .append(Estilo.esc(explicacion)).append("</td></tr>");
    }

    private JPanel pestanaCorreo() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        JLabel l = new JLabel("Correo:");
        l.setFont(Estilo.NEGRITA);
        correo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.BORDE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JButton verificar = Estilo.boton("Verificar", true);
        verificar.addActionListener(e -> verificarCorreo());
        fila.add(l);
        fila.add(correo);
        fila.add(verificar);
        consentimiento.setOpaque(false);

        JPanel form = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        form.setBackground(Estilo.SUPERFICIE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        form.add(Estilo.parrafo("Comprueba si tu correo aparece en bases de datos robadas publicadas en Internet. "
                + "El correo solo se envía si das tu autorización."));
        form.add(fila);
        form.add(consentimiento);

        JPanel p = new JPanel(new BorderLayout());
        p.add(form, BorderLayout.NORTH);
        p.add(Estilo.scroll(resultadoCorreo), BorderLayout.CENTER);
        return p;
    }

    private void verificarCorreo() {
        String c = correo.getText();
        boolean ok = consentimiento.isSelected();
        try {
            gestor.validarConsultaCorreo(c, ok);
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
            return;
        }
        Estilo.ponerHtml(resultadoCorreo, "<p>Consultando el servicio de filtraciones...</p>");
        new SwingWorker<ConsultaCorreo, Void>() {
            private String error;

            @Override
            protected ConsultaCorreo doInBackground() {
                try {
                    return gestor.consultarCorreo(est, c, ok);
                } catch (ValidacionException | ServicioNoDisponibleException ex) {
                    error = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                ConsultaCorreo r;
                try {
                    r = get();
                } catch (Exception ex) {
                    r = null;
                    error = ex.getMessage();
                }
                if (r == null) {
                    Estilo.ponerHtml(resultadoCorreo, Estilo.aviso(Estilo.esc(error), Estilo.NARANJA));
                    return;
                }
                ctx.guardar();
                consentimiento.setSelected(false);
                StringBuilder sb = new StringBuilder();
                if (r.getFiltraciones().isEmpty()) {
                    sb.append(Estilo.aviso("Tu correo no aparece en filtraciones conocidas.", Estilo.VERDE));
                } else {
                    sb.append(Estilo.aviso("Tu correo aparece en " + r.getFiltraciones().size() + " filtraciones. Nivel de exposición: "
                            + Estilo.esc(r.getClasificacion()), Estilo.colorNivel(r.getClasificacion())));
                    sb.append(Estilo.h("Sitios donde se filtró")).append(Estilo.lista(r.getFiltraciones()));
                }
                sb.append("<p style='color:#A3A3B8'>Correo consultado: ").append(Estilo.esc(r.getCorreoEnmascarado()))
                  .append(" · Fuente: ").append(Estilo.esc(r.getFuente())).append("</p>");
                sb.append(Estilo.h("Recomendaciones")).append(Estilo.lista(r.getRecomendaciones()));
                Estilo.ponerHtml(resultadoCorreo, sb.toString());
            }
        }.execute();
    }

    @Override
    public void actualizar() {
        if (correo.getText().isBlank()) correo.setText(est.getCorreo());
    }
}
