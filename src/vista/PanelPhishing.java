package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import modelo.AnalisisMensaje;
import modelo.Estudiante;
import modelo.Evaluacion;
import modelo.Indicador;
import negocio.GestorPhishing;
import negocio.ValidacionException;

public class PanelPhishing extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;
    private static final String[] CANALES = {"Correo", "Mensajería/SMS", "Red social"};
    private static final String[] COPIA_SEGURA = {
        "Copia el texto del mensaje y pégalo aquí. No respondas al mensaje.",
        "Enlaces: clic derecho > «Copiar dirección del enlace». Nunca hagas clic en el enlace.",
        "Si el enlace es una imagen o un botón, no lo toques: pasa el mouse encima y anota la dirección que aparece.",
        "Adjuntos: NO los descargues ni los abras. Escudo Digital no analiza archivos adjuntos.",
        "VirusTotal visita los enlaces desde sus propios servidores; tu equipo no abre la página."
    };

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorPhishing gestor;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JComboBox<String> canal = new JComboBox<>(CANALES);
    private final JTextField remitente = new JTextField(30);
    private final JTextField asunto = new JTextField(30);
    private final JTextArea cuerpo = new JTextArea(8, 40);
    private final JTextArea enlaces = new JTextArea(3, 40);
    private final JTextField enlaceSolo = Estilo.campo(new JTextField(40));
    private final JButton analizarEnlace = Estilo.boton("Analizar enlace", true);
    private final javax.swing.JEditorPane resultadoEnlace = Estilo.areaHtml("");
    private final JButton analizar = Estilo.boton("Analizar mensaje", true);
    private final JTable tabla = Estilo.tabla("Código", "Fecha", "Tipo", "Remitente o enlace", "Puntaje", "Clasificación");

    public PanelPhishing(ContextoAplicacion ctx, Estudiante est) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorPhishing();
        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Analizar un mensaje sospechoso",
                "Pega un correo o mensaje que te parezca extraño y Escudo Digital buscará señales de phishing."), BorderLayout.NORTH);
        tabs.addTab("1. Copiar sin riesgo", copiaSegura());
        tabs.addTab("2. Analizar un mensaje", formulario());
        tabs.addTab("3. Analizar solo un enlace", soloEnlace());
        tabs.addTab("4. Historial", historial());
        c.add(tabs, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    private JPanel copiaSegura() {
        JPanel p = new JPanel(new BorderLayout());
        StringBuilder sb = new StringBuilder(Estilo.h("Antes de analizar, copia el mensaje de forma segura"));
        sb.append("<ol>");
        for (String s : COPIA_SEGURA) sb.append("<li style='margin-bottom:8px'>").append(Estilo.esc(s)).append("</li>");
        sb.append("</ol>");
        p.add(Estilo.scroll(Estilo.areaHtml(sb.toString())), BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton seguir = Estilo.boton("Entendido, analizar un mensaje", true);
        JButton soloLink = Estilo.boton("Solo tengo un enlace", false);
        soloLink.addActionListener(e -> tabs.setSelectedIndex(2));
        sur.add(soloLink);
        seguir.addActionListener(e -> tabs.setSelectedIndex(1));
        sur.add(seguir);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel formulario() {
        JPanel f = new JPanel(new GridBagLayout());
        f.setBackground(Estilo.SUPERFICIE);
        f.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = -1;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 10);
        cuerpo.setLineWrap(true);
        cuerpo.setWrapStyleWord(true);
        enlaces.setLineWrap(true);
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        cuerpo.setFont(mono);
        enlaces.setFont(mono);
        fila(f, g, "Canal", canal, 0);
        fila(f, g, "Remitente (opcional)", remitente, 0);
        fila(f, g, "", Estilo.ayudaAncho("Correo, número o nombre que aparece en el mensaje. Si no lo sabes, déjalo vacío.", 360), 0);
        fila(f, g, "Asunto (opcional)", asunto, 0);
        fila(f, g, "Cuerpo del mensaje", Estilo.scroll(cuerpo), 1);
        fila(f, g, "Enlaces copiados sin abrir (uno por línea)", Estilo.scroll(enlaces), 0.3);

        JPanel p = new JPanel(new BorderLayout());
        javax.swing.JScrollPane sf = Estilo.scroll(f);
        sf.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        p.add(sf, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        JLabel vt = Estilo.ayuda(gestor.virusTotalDisponible()
                ? "VirusTotal está activo: revisará hasta " + GestorPhishing.MAX_ENLACES_VIRUSTOTAL + " enlaces (puede tardar hasta un minuto)."
                : "VirusTotal no está configurado: se hará solo el análisis de texto.");
        vt.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        JButton limpiar = Estilo.boton("Limpiar", false);
        limpiar.addActionListener(e -> limpiar());
        analizar.addActionListener(e -> analizar());
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        b.add(limpiar);
        b.add(analizar);
        sur.add(vt, BorderLayout.CENTER);
        sur.add(b, BorderLayout.EAST);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void limpiar() {
        remitente.setText("");
        asunto.setText("");
        cuerpo.setText("");
        enlaces.setText("");
    }

    private void analizar() {
        String can = (String) canal.getSelectedItem();
        String rem = remitente.getText();
        String asu = asunto.getText();
        String cue = cuerpo.getText();
        List<String> enl = new ArrayList<>();
        for (String l : enlaces.getText().split("\\R")) if (!l.isBlank()) enl.add(l.trim());
        analizar.setEnabled(false);
        analizar.setText("Analizando...");
        new SwingWorker<AnalisisMensaje, Void>() {
            private String error;

            @Override
            protected AnalisisMensaje doInBackground() {
                try {
                    return gestor.analizarMensaje(est, can, rem, asu, cue, enl);
                } catch (ValidacionException ex) {
                    error = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                analizar.setEnabled(true);
                analizar.setText("Analizar mensaje");
                AnalisisMensaje a;
                try {
                    a = get();
                } catch (Exception ex) {
                    a = null;
                    error = ex.getMessage();
                }
                if (a == null) {
                    Estilo.error(PanelPhishing.this, error);
                    return;
                }
                ctx.guardar();
                actualizar();
                Estilo.mostrarHtml(PanelPhishing.this, "Resultado del análisis", detalle(a));
            }
        }.execute();
    }

    private JPanel soloEnlace() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);
        JLabel l = new JLabel("Enlace:");
        l.setFont(Estilo.NEGRITA);
        fila.add(l);
        fila.add(enlaceSolo);
        fila.add(analizarEnlace);
        JPanel form = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
        form.setBackground(Estilo.SUPERFICIE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));
        form.add(Estilo.parrafo("Pega un enlace copiado sin abrirlo (clic derecho > «Copiar dirección del enlace»). "
                + "No necesitas el texto del mensaje: VirusTotal lo revisa desde sus servidores y tu equipo no abre la página."));
        form.add(fila);
        form.add(Estilo.parrafo(gestor.virusTotalDisponible()
                ? "VirusTotal está activo. Si el enlace es nuevo, el análisis puede tardar hasta un minuto."
                : "VirusTotal no está configurado: solo se revisará el formato del enlace (IP, acortador, http y dominios que imitan a otros)."));
        analizarEnlace.addActionListener(e -> analizarEnlace());
        enlaceSolo.addActionListener(e -> analizarEnlace());
        JPanel p = new JPanel(new BorderLayout());
        p.add(form, BorderLayout.NORTH);
        p.add(Estilo.scroll(resultadoEnlace), BorderLayout.CENTER);
        return p;
    }

    private void analizarEnlace() {
        String url = enlaceSolo.getText();
        analizarEnlace.setEnabled(false);
        analizarEnlace.setText("Analizando...");
        Estilo.ponerHtml(resultadoEnlace, Estilo.suave("Analizando el enlace..."));
        new SwingWorker<AnalisisMensaje, Void>() {
            private String error;

            @Override
            protected AnalisisMensaje doInBackground() {
                try {
                    return gestor.analizarEnlace(est, url);
                } catch (ValidacionException ex) {
                    error = ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                analizarEnlace.setEnabled(true);
                analizarEnlace.setText("Analizar enlace");
                AnalisisMensaje a;
                try {
                    a = get();
                } catch (Exception ex) {
                    a = null;
                    error = ex.getMessage();
                }
                if (a == null) {
                    Estilo.ponerHtml(resultadoEnlace, Estilo.aviso(Estilo.esc(error), Estilo.NARANJA));
                    return;
                }
                ctx.guardar();
                actualizar();
                enlaceSolo.setText("");
                Estilo.ponerHtml(resultadoEnlace, detalle(a));
            }
        }.execute();
    }

    static String detalle(AnalisisMensaje a) {
        StringBuilder sb = new StringBuilder();
        sb.append(Estilo.aviso(String.format("Puntaje de sospecha: %.0f / 100 &nbsp;·&nbsp; %s", a.getPuntaje(),
                Estilo.esc(a.getClasificacion())), Estilo.colorNivel(a.getClasificacion())));
        if (a.esSoloEnlace()) {
            sb.append("<p>Enlace analizado: <b>").append(Estilo.esc(a.getEnlaces().get(0))).append("</b></p>");
        } else {
            sb.append("<p>Remitente: <b>").append(Estilo.esc(a.getRemitente())).append("</b> · Canal: ")
              .append(Estilo.esc(a.getCanal())).append("</p>");
        }
        if (!a.esSoloEnlace() && !a.getRemitente().contains("@")) {
            sb.append("<p style='color:#A3A3B8'>Sin el correo del remitente no se pudo revisar si su dominio imita a uno legítimo (I1 e I2).</p>");
        }
        sb.append(Estilo.h("Señales encontradas"));
        if (a.getIndicadores().isEmpty()) {
            sb.append("<p>No se encontraron señales de phishing.</p>");
        } else {
            sb.append("<table cellpadding='5'>");
            for (Indicador i : a.getIndicadores()) {
                sb.append("<tr><td valign='top' style='color:#FF5C6C;font-weight:bold'>").append(Estilo.esc(i.getCodigo()))
                  .append(" +").append(i.getPeso()).append("</td><td>").append(Estilo.esc(i.getDescripcion())).append("</td></tr>");
            }
            sb.append("</table>");
        }
        if (!a.getResultadosVirusTotal().isEmpty()) {
            sb.append(Estilo.h("VirusTotal")).append(Estilo.lista(a.getResultadosVirusTotal()));
        }
        sb.append(Estilo.h("Recomendaciones")).append(Estilo.lista(a.getRecomendaciones()));
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
            AnalisisMensaje a = seleccionado();
            if (a != null) Estilo.mostrarHtml(this, "Análisis " + a.getId(), detalle(a));
        });
        eliminar.addActionListener(e -> {
            AnalisisMensaje a = seleccionado();
            if (a == null || !Estilo.confirmar(this, "¿Eliminar el análisis " + a.getId() + "?")) return;
            try {
                gestor.eliminar(est, a.getId());
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

    private AnalisisMensaje seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.info(this, "Selecciona un análisis de la tabla.");
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
        for (AnalisisMensaje a : gestor.listar(est)) {
            m.addRow(new Object[] {a.getId(), a.getFecha().format(Evaluacion.FORMATO_FECHA), a.esSoloEnlace() ? "Enlace" : "Mensaje",
                a.esSoloEnlace() ? a.getEnlaces().get(0) : a.getRemitente(),
                String.format("%.0f", a.getPuntaje()), a.getClasificacion()});
        }
    }

    private static void fila(JPanel f, GridBagConstraints g, String etiqueta, java.awt.Component campo, double peso) {
        g.gridy++;
        g.gridx = 0;
        g.weightx = 0;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        JLabel l = new JLabel(etiqueta);
        l.setFont(Estilo.NEGRITA);
        f.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.weighty = peso;
        g.fill = peso > 0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        f.add(campo, g);
    }
}
