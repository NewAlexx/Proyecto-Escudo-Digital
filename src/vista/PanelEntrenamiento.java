package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import modelo.Escenario;
import modelo.Estudiante;
import modelo.SesionEntrenamiento;
import negocio.GestorEntrenamiento;
import negocio.ValidacionException;

public class PanelEntrenamiento extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorEntrenamiento gestor;
    private final JTabbedPane tabs = new JTabbedPane();

    private final CardLayout pasos = new CardLayout();
    private final JPanel zona = new JPanel(pasos);
    private final JLabel contador = Estilo.subtitulo("");
    private final JProgressBar progreso = new JProgressBar(0, SesionEntrenamiento.ESCENARIOS_POR_SESION);
    private final JTextArea mensaje = new JTextArea();
    private final JEditorPane retro = Estilo.areaHtml("");
    private final JButton esPhishing = Estilo.botonPeligro("Es phishing");
    private final JButton esLegitimo = Estilo.boton("Es legítimo", true);
    private final JButton siguiente = Estilo.boton("Siguiente", true);
    private final JEditorPane resumenFinal = Estilo.areaHtml("");
    private final JLabel inicioInfo = Estilo.ayuda("");
    private SesionEntrenamiento sesion;
    private Escenario escenario;

    public PanelEntrenamiento(ContextoAplicacion ctx, Estudiante est) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorEntrenamiento();
        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Entrenamiento antiphishing",
                "Aprende a reconocer el phishing y practica con mensajes simulados. La dificultad se adapta a tus aciertos."),
                BorderLayout.NORTH);
        tabs.addTab("1. ¿Cómo reconocer un phishing? (tutorial)", tutorial());
        tabs.addTab("2. Cuestionario (10 preguntas)", cuestionario());
        c.add(tabs, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    private JPanel tutorial() {
        StringBuilder sb = new StringBuilder(Estilo.h("Nueve señales para reconocer un phishing"));
        sb.append("<table cellpadding='6'>");
        for (String[] paso : GestorEntrenamiento.TUTORIAL) {
            sb.append("<tr><td><b style='color:#D9A6F2'>").append(Estilo.esc(paso[0])).append("</b><br>")
              .append(Estilo.esc(paso[1])).append("</td></tr>");
        }
        sb.append("</table>");
        JPanel p = new JPanel(new BorderLayout());
        p.add(Estilo.scroll(Estilo.areaHtml(sb.toString())), BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton practicar = Estilo.boton("Practicar con el cuestionario", true);
        practicar.addActionListener(e -> tabs.setSelectedIndex(1));
        sur.add(practicar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel cuestionario() {
        JPanel inicio = Estilo.tarjeta(new BorderLayout(0, 12));
        inicio.add(Estilo.subtitulo("Cuestionario de 10 preguntas"), BorderLayout.NORTH);
        inicio.add(inicioInfo, BorderLayout.CENTER);
        JButton empezar = Estilo.boton("Empezar cuestionario", true);
        empezar.addActionListener(e -> empezar());
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        b.setOpaque(false);
        b.add(empezar);
        inicio.add(b, BorderLayout.SOUTH);
        JPanel envInicio = new JPanel(new BorderLayout());
        envInicio.add(inicio, BorderLayout.NORTH);

        JPanel pregunta = new JPanel(new BorderLayout(0, 10));
        JPanel arriba = new JPanel(new BorderLayout(0, 6));
        arriba.add(contador, BorderLayout.NORTH);
        progreso.setStringPainted(false);
        arriba.add(progreso, BorderLayout.SOUTH);
        pregunta.add(arriba, BorderLayout.NORTH);
        mensaje.setEditable(false);
        mensaje.setLineWrap(true);
        mensaje.setWrapStyleWord(true);
        mensaje.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        mensaje.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        mensaje.setBackground(Estilo.SUPERFICIE_ALTA);
        mensaje.setForeground(Estilo.TEXTO);
        JPanel medio = new JPanel(new BorderLayout(0, 8));
        medio.add(Estilo.scroll(mensaje), BorderLayout.CENTER);
        javax.swing.JScrollPane sr = Estilo.scroll(retro);
        sr.setPreferredSize(new java.awt.Dimension(400, 120));
        medio.add(sr, BorderLayout.SOUTH);
        pregunta.add(medio, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton abandonar = Estilo.boton("Abandonar", false);
        acciones.add(abandonar);
        acciones.add(esLegitimo);
        acciones.add(esPhishing);
        acciones.add(siguiente);
        pregunta.add(acciones, BorderLayout.SOUTH);
        esPhishing.addActionListener(e -> responder(true));
        esLegitimo.addActionListener(e -> responder(false));
        siguiente.addActionListener(e -> avanzar());
        abandonar.addActionListener(e -> abandonar());

        JPanel fin = new JPanel(new BorderLayout(0, 8));
        fin.add(Estilo.scroll(resumenFinal), BorderLayout.CENTER);
        JPanel bf = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton otra = Estilo.boton("Volver al inicio del cuestionario", true);
        otra.addActionListener(e -> mostrarInicio());
        bf.add(otra);
        fin.add(bf, BorderLayout.SOUTH);

        zona.add(envInicio, "inicio");
        zona.add(pregunta, "pregunta");
        zona.add(fin, "fin");
        zona.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return zona;
    }

    private void mostrarInicio() {
        inicioInfo.setText("<html>Verás 10 mensajes simulados. En cada uno decide si es <b>phishing</b> o <b>legítimo</b>. "
                + "Dos aciertos seguidos suben la dificultad y un error la baja.<br><br>"
                + "Nivel inicial según tu último diagnóstico: <b>" + gestor.calcularNivelInicial(est) + "</b> · "
                + "Cuestionarios de hoy: <b>" + gestor.sesionesDeHoy(est) + " de " + GestorEntrenamiento.MAX_SESIONES_DIARIAS
                + "</b>.<br>Un cuestionario abandonado no se guarda.</html>");
        pasos.show(zona, "inicio");
    }

    private void empezar() {
        try {
            sesion = gestor.iniciarSesion(est);
            avanzar();
            pasos.show(zona, "pregunta");
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void avanzar() {
        if (sesion.estaCompleta()) {
            terminar();
            return;
        }
        escenario = gestor.siguienteEscenario(sesion);
        int n = sesion.getCantidadPresentados() + 1;
        contador.setText("Pregunta " + n + " de 10 · nivel " + escenario.getDificultad());
        progreso.setValue(n - 1);
        mensaje.setText(escenario.getTexto());
        mensaje.setCaretPosition(0);
        Estilo.ponerHtml(retro, "<p style='color:#A3A3B8'>Lee el mensaje con atención y elige una respuesta.</p>");
        esPhishing.setEnabled(true);
        esLegitimo.setEnabled(true);
        siguiente.setEnabled(false);
    }

    private void responder(boolean marcoPhishing) {
        int nivelAntes = sesion.getNivelActual();
        boolean correcta = gestor.responder(sesion, escenario, marcoPhishing);
        String txt;
        if (correcta) {
            txt = Estilo.aviso("¡Correcto! Este mensaje era " + (escenario.esPhishing() ? "phishing" : "legítimo") + ".", Estilo.VERDE);
        } else if (escenario.esPhishing()) {
            txt = Estilo.aviso("Incorrecto: marcaste como legítimo un mensaje fraudulento. En una situación real habrías caído en el engaño.",
                    Estilo.ROJO);
        } else {
            txt = Estilo.aviso("Incorrecto: este mensaje era legítimo.", Estilo.NARANJA);
        }
        txt += "<p>" + Estilo.esc(escenario.getExplicacion()) + "</p>";
        if (sesion.getNivelActual() > nivelAntes && !sesion.estaCompleta()) {
            txt += "<p><b>¡Subiste al nivel " + sesion.getNivelActual() + "!</b> Los siguientes mensajes serán más difíciles.</p>";
        }
        Estilo.ponerHtml(retro, txt);
        progreso.setValue(sesion.getCantidadPresentados());
        esPhishing.setEnabled(false);
        esLegitimo.setEnabled(false);
        siguiente.setEnabled(true);
        siguiente.setText(sesion.estaCompleta() ? "Ver resultado" : "Siguiente");
    }

    private void abandonar() {
        if (!Estilo.confirmar(this, "Si abandonas, este cuestionario no se guardará. ¿Deseas salir?")) return;
        try {
            gestor.finalizar(est, sesion);
        } catch (ValidacionException ex) {
            Estilo.info(this, ex.getMessage());
        }
        sesion = null;
        mostrarInicio();
    }

    private void terminar() {
        try {
            gestor.finalizar(est, sesion);
            ctx.guardar();
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
            mostrarInicio();
            return;
        }
        double ir = gestor.calcularResiliencia(est);
        String nivel = GestorEntrenamiento.nivelResiliencia(ir);
        StringBuilder sb = new StringBuilder();
        sb.append(Estilo.aviso(String.format("Cuestionario completado: %d aciertos de 10 · Puntaje %.2f / 100",
                sesion.getAciertos(), sesion.getPuntaje()), Estilo.colorNivel(sesion.getClasificacion())));
        sb.append(String.format("<p>Índice de Resiliencia: <b>%.2f</b> (%s)</p>", ir, Estilo.esc(nivel)));
        sb.append(String.format("<p>Falsos positivos (legítimos marcados como phishing): %d · Falsos negativos (phishing no detectado): %d</p>",
                sesion.getFalsosPositivos(), sesion.getFalsosNegativos()));
        sb.append(Estilo.h("Recomendaciones")).append(Estilo.lista(sesion.getRecomendaciones()));
        sb.append("<p style='color:#A3A3B8'>Tu historial de cuestionarios y la evolución del Índice de Resiliencia están en "
                + "«Índice global de riesgo» › «Índice de Resiliencia».</p>");
        Estilo.ponerHtml(resumenFinal, sb.toString());
        siguiente.setText("Siguiente");
        pasos.show(zona, "fin");
    }

    @Override
    public void actualizar() {
        if (sesion == null || sesion.estaCompleta()) mostrarInicio();
    }
}
