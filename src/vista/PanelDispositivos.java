package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.Dispositivo;
import modelo.Estudiante;
import negocio.GestorDispositivos;
import negocio.ValidacionException;

public class PanelDispositivos extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorDispositivos gestor;
    private final JTable tabla = Estilo.tabla("Alias", "Tipo", "Sistema operativo", "Última actualización", "IE", "Nivel", "Alerta");

    public PanelDispositivos(ContextoAplicacion ctx, Estudiante est) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorDispositivos();
        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Mis dispositivos",
                "Registra hasta " + GestorDispositivos.MAX_DISPOSITIVOS + " dispositivos y conoce su Índice de Exposición (IE). "
                + "Si no sabes un dato, puedes responder «No sé»."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        JButton registrar = Estilo.boton("Registrar dispositivo", true);
        JButton detalle = Estilo.boton("Ver detalle y recomendaciones", false);
        JButton editar = Estilo.boton("Actualizar", false);
        JButton eliminar = Estilo.boton("Eliminar", false);
        botones.add(registrar);
        botones.add(detalle);
        botones.add(editar);
        botones.add(eliminar);
        botones.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        centro.add(botones, BorderLayout.SOUTH);
        c.add(centro, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);

        registrar.addActionListener(e -> {
            if (gestor.listar(est).size() >= GestorDispositivos.MAX_DISPOSITIVOS) {
                Estilo.error(this, "Alcanzaste el máximo de " + GestorDispositivos.MAX_DISPOSITIVOS + " dispositivos.");
                return;
            }
            Dispositivo d = new DialogoDispositivo(this, gestor, est, null).abrir();
            if (d != null) {
                ctx.guardar();
                actualizar();
                Estilo.mostrarHtml(this, "Dispositivo registrado", detalle(d));
            }
        });
        detalle.addActionListener(e -> {
            Dispositivo d = seleccionado();
            if (d != null) Estilo.mostrarHtml(this, d.getAlias(), detalle(d));
        });
        editar.addActionListener(e -> {
            Dispositivo d = seleccionado();
            if (d == null) return;
            if (new DialogoDispositivo(this, gestor, est, d).abrir() != null) {
                ctx.guardar();
                actualizar();
                Estilo.mostrarHtml(this, "Dispositivo actualizado", detalle(d));
            }
        });
        eliminar.addActionListener(e -> {
            Dispositivo d = seleccionado();
            if (d == null || !Estilo.confirmar(this, "¿Eliminar el dispositivo «" + d.getAlias() + "»?")) return;
            try {
                gestor.eliminar(est, d.getAlias());
                ctx.guardar();
                actualizar();
            } catch (ValidacionException ex) {
                Estilo.error(this, ex.getMessage());
            }
        });
    }

    static String detalle(Dispositivo d) {
        StringBuilder sb = new StringBuilder();
        sb.append(Estilo.aviso(String.format("Índice de Exposición: %.0f / 100 &nbsp;·&nbsp; %s", d.getPuntaje(),
                Estilo.esc(d.getClasificacion())), Estilo.colorNivel(d.getClasificacion())));
        sb.append("<table cellpadding='4'>")
          .append("<tr><td><b>Tipo</b></td><td>").append(Estilo.esc(d.getTipoDispositivo().toString())).append("</td></tr>")
          .append("<tr><td><b>Sistema operativo</b></td><td>").append(Estilo.esc(d.getSistemaOperativo())).append("</td></tr>")
          .append("<tr><td><b>Última actualización</b></td><td>").append(d.conoceFechaActualizacion()
                  ? d.getDiasSinActualizar() + " días sin actualizar" : "No sé").append("</td></tr>")
          .append("<tr><td><b>Uso de WiFi pública</b></td><td>").append(d.getWifiPublica()).append("</td></tr>")
          .append("</table>");
        if (Dispositivo.esSistemaSinSoporte(d.getSistemaOperativo())) {
            sb.append(Estilo.aviso("Tu sistema operativo ya no recibe actualizaciones de seguridad. Exposición: Crítica.", Estilo.ROJO));
        }
        sb.append(Estilo.h("Recomendaciones")).append(Estilo.lista(d.getRecomendaciones()));
        return sb.toString();
    }

    private Dispositivo seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.info(this, "Selecciona un dispositivo de la tabla.");
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
        for (Dispositivo d : gestor.listar(est)) {
            m.addRow(new Object[] {d.getAlias(), d.getTipoDispositivo(), d.getSistemaOperativo(),
                d.conoceFechaActualizacion() ? d.getDiasSinActualizar() + " días" : "No sé",
                String.format("%.0f", d.getPuntaje()), d.getClasificacion(), d.tieneAlerta() ? "Sí" : ""});
        }
    }
}
