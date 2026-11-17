package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import modelo.AnalisisMensaje;
import modelo.ConsultaCorreo;
import modelo.DiagnosticoHigiene;
import modelo.Dispositivo;
import modelo.Estudiante;
import modelo.EvaluacionContrasena;
import modelo.EvaluacionGlobal;
import modelo.SesionEntrenamiento;

public class PanelInicio extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final Estudiante est;
    private final VentanaPrincipal ventana;
    private final JPanel resumen = new JPanel(new GridLayout(2, 4, 12, 12));
    private final JLabel saludo = Estilo.titulo("");

    public PanelInicio(ContextoAplicacion ctx, Estudiante est, VentanaPrincipal ventana) {
        super(new BorderLayout());
        this.est = est;
        this.ventana = ventana;
        JPanel c = Estilo.contenedor();
        JPanel arriba = new JPanel(new BorderLayout(0, 4));
        arriba.setOpaque(false);
        arriba.add(saludo, BorderLayout.NORTH);
        arriba.add(Estilo.parrafo("Este es el resumen de tu seguridad digital. Elige una tarjeta o una opción del menú para empezar. "
                + "Para un índice global completo, realiza al menos una evaluación de cada módulo."), BorderLayout.CENTER);
        arriba.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        c.add(arriba, BorderLayout.NORTH);
        resumen.setOpaque(false);
        c.add(resumen, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
        saludo.setText("Hola, " + est.getNombres().split(" ")[0]);
        resumen.removeAll();
        DiagnosticoHigiene d = est.getUltimaEvaluacion(DiagnosticoHigiene.class);
        resumen.add(tarjeta("Higiene digital", d == null ? "Sin diagnóstico" : String.format("%.1f / 100", d.getIHD()),
                d == null ? "10 preguntas" : d.getClasificacion(), d == null ? null : d.getClasificacion(), "diagnostico"));
        EvaluacionContrasena c = est.getUltimaEvaluacion(EvaluacionContrasena.class);
        resumen.add(tarjeta("Contraseña", c == null ? "Sin evaluar" : String.format("%.0f / 100", c.getPuntaje()),
                c == null ? "Evalúala" : c.getClasificacion(), c == null ? null : c.getClasificacion(), "credenciales"));
        ConsultaCorreo co = est.getUltimaEvaluacion(ConsultaCorreo.class);
        resumen.add(tarjeta("Correo filtrado", co == null ? "Sin consultar" : co.getFiltraciones().size() + " filtraciones",
                co == null ? "Pendiente" : co.getClasificacion(), co == null ? null : co.getClasificacion(), "credenciales"));
        int nd = est.getEvaluaciones(Dispositivo.class).size();
        double maxIe = est.getEvaluaciones(Dispositivo.class).stream().mapToDouble(Dispositivo::getPuntaje).max().orElse(-1);
        resumen.add(tarjeta("Dispositivos", nd + " de 5",
                nd == 0 ? "Registra uno" : String.format("IE máx.: %.0f", maxIe),
                nd == 0 ? null : (maxIe >= 75 ? "Crítico" : maxIe >= 50 ? "Alto" : maxIe >= 25 ? "Medio" : "Bajo"), "dispositivos"));
        AnalisisMensaje am = est.getUltimaEvaluacion(AnalisisMensaje.class);
        resumen.add(tarjeta("Mensaje sospechoso", am == null ? "Sin analizar" : String.format("%.0f / 100", am.getPuntaje()),
                am == null ? "Analiza uno" : am.getClasificacion(), am == null ? null : am.getClasificacion(), "phishing"));
        SesionEntrenamiento s = est.getUltimaEvaluacion(SesionEntrenamiento.class);
        resumen.add(tarjeta("Entrenamiento", s == null ? "Sin cuestionarios" : String.format("%.1f / 100", s.getPuntaje()),
                s == null ? "10 mensajes" : s.getAciertos() + "/10 aciertos", null, "entrenamiento"));
        EvaluacionGlobal g = est.getUltimaEvaluacion(EvaluacionGlobal.class);
        resumen.add(tarjeta("Riesgo global", g == null ? "Sin calcular" : String.format("%.1f / 100", g.getPuntaje()),
                g == null ? "Calcúlalo" : g.getClasificacion(), g == null ? null : g.getClasificacion(), "riesgo"));
        resumen.add(tarjeta("Mi cuenta", est.getTodasLasEvaluaciones().size() + " resultados",
                "En este equipo", null, "perfil"));
        resumen.revalidate();
        resumen.repaint();
    }

    private JPanel tarjeta(String titulo, String valor, String detalle, String nivel, String destino) {
        Color color = nivel == null ? Estilo.ACENTO_CLARO : Estilo.colorNivel(nivel);
        JPanel t = Estilo.tarjeta(new BorderLayout(0, 6)).conAcento(nivel == null ? Estilo.ACENTO : color);
        JLabel tt = new JLabel(titulo);
        tt.setFont(Estilo.NEGRITA);
        tt.setForeground(Estilo.TEXTO_SUAVE);
        JLabel v = new JLabel(valor);
        v.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 22));
        v.setForeground(color);
        JLabel dd = new JLabel(detalle);
        dd.setFont(Estilo.NORMAL);
        JButton ir = Estilo.boton("Abrir", false);
        ir.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        ir.setFont(Estilo.PEQUENA.deriveFont(java.awt.Font.BOLD));
        ir.addActionListener(e -> ventana.mostrar(destino));
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.add(dd, BorderLayout.CENTER);
        sur.add(ir, BorderLayout.EAST);
        t.add(tt, BorderLayout.NORTH);
        t.add(v, BorderLayout.CENTER);
        t.add(sur, BorderLayout.SOUTH);
        return t;
    }
}
