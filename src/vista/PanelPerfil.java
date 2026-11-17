package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.Estudiante;
import negocio.GestorUsuarios;
import negocio.ValidacionException;

public class PanelPerfil extends JPanel implements Actualizable {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final VentanaPrincipal ventana;
    private final JTextField nombres = new JTextField(28);
    private final JTextField correo = new JTextField(28);
    private final JTextField carrera = new JTextField(28);
    private final JComboBox<Integer> semestre = new JComboBox<>();

    public PanelPerfil(ContextoAplicacion ctx, Estudiante est, VentanaPrincipal ventana) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.est = est;
        this.ventana = ventana;
        for (int i = GestorUsuarios.SEMESTRE_MIN; i <= GestorUsuarios.SEMESTRE_MAX; i++) semestre.addItem(i);
        correo.setEditable(false);

        JPanel c = Estilo.contenedor();
        c.add(Estilo.encabezado("Mi perfil",
                "Consulta y actualiza tus datos. El correo y la contraseña no se pueden cambiar."), BorderLayout.NORTH);

        JPanel datos = Estilo.tarjeta(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(6, 0, 6, 14);
        g.gridy = -1;
        fila(datos, g, "Nombres y apellidos", nombres);
        fila(datos, g, "Correo institucional", correo);
        fila(datos, g, "Carrera", carrera);
        fila(datos, g, "Semestre", semestre);
        g.gridy++;
        g.gridx = 1;
        JButton guardar = Estilo.boton("Guardar cambios", true);
        guardar.addActionListener(e -> guardar());
        datos.add(guardar, g);

        JPanel peligro = Estilo.tarjeta(new BorderLayout(0, 8)).conBorde(Estilo.ROJO);
        JLabel tp = Estilo.subtitulo("Eliminar mi cuenta");
        tp.setForeground(Estilo.ROJO);
        peligro.add(tp, BorderLayout.NORTH);
        peligro.add(Estilo.parrafo("Se borrarán tu cuenta, todos tus resultados y el archivo de datos de este equipo. "
                + "Esta acción no se puede deshacer."), BorderLayout.CENTER);
        JButton eliminar = Estilo.botonPeligro("Eliminar mi cuenta y todos mis datos");
        eliminar.addActionListener(e -> eliminar());
        JPanel be = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        be.setOpaque(false);
        be.add(eliminar);
        peligro.add(be, BorderLayout.SOUTH);

        JPanel columna = new JPanel(new BorderLayout(0, 18));
        columna.setOpaque(false);
        columna.add(datos, BorderLayout.NORTH);
        columna.add(peligro, BorderLayout.CENTER);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(columna, BorderLayout.NORTH);
        c.add(envoltorio, BorderLayout.CENTER);
        add(c, BorderLayout.CENTER);
    }

    private void guardar() {
        try {
            ctx.getGestorUsuarios().actualizarPerfil(nombres.getText(), carrera.getText(), (Integer) semestre.getSelectedItem());
            ctx.guardar();
            Estilo.info(this, "Datos actualizados.");
            ventana.mostrar("perfil");
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        if (!Estilo.confirmar(this, "Se borrarán tu cuenta, todos tus resultados y el archivo de datos del equipo.\n¿Confirmas?")) return;
        try {
            ctx.getGestorUsuarios().eliminarCuenta();
            ctx.guardar();
            Estilo.info(this, "Cuenta y datos eliminados de este equipo.");
            ventana.dispose();
            new VentanaAcceso(ctx).setVisible(true);
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    @Override
    public void actualizar() {
        nombres.setText(est.getNombres());
        correo.setText(est.getCorreo());
        carrera.setText(est.getCarrera());
        semestre.setSelectedItem(est.getSemestre());
    }

    private static void fila(JPanel p, GridBagConstraints g, String etiqueta, java.awt.Component campo) {
        g.gridy++;
        g.gridx = 0;
        JLabel l = new JLabel(etiqueta);
        l.setFont(Estilo.NEGRITA);
        p.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(campo, g);
        g.weightx = 0;
    }
}
