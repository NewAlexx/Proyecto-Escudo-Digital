package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import modelo.Dispositivo;
import modelo.Estudiante;
import modelo.FrecuenciaWifi;
import modelo.TipoDispositivo;
import negocio.GestorDispositivos;
import negocio.ValidacionException;

public class DialogoDispositivo extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final GestorDispositivos gestor;
    private final Estudiante est;
    private final Dispositivo actual;
    private Dispositivo resultado;

    private final JTextField alias = new JTextField(22);
    private final JComboBox<TipoDispositivo> tipo = new JComboBox<>(TipoDispositivo.values());
    private final JLabel guia = Estilo.ayuda("");
    private final JTextField so = new JTextField(22);
    private final JCheckBox soNoSe = new JCheckBox("No sé");
    private final JTextField fecha = new JTextField(10);
    private final JCheckBox fechaNoSe = new JCheckBox("No sé");
    private final JCheckBox antimalware = new JCheckBox();
    private final JCheckBox cifrado = new JCheckBox("Tiene el almacenamiento cifrado");
    private final JCheckBox bloqueo = new JCheckBox("Tiene bloqueo de pantalla");
    private final JCheckBox compartido = new JCheckBox("Lo comparto con otras personas");
    private final JRadioButton[] wifi = new JRadioButton[5];

    public DialogoDispositivo(Component padre, GestorDispositivos gestor, Estudiante est, Dispositivo actual) {
        super(SwingUtilities.getWindowAncestor(padre), actual == null ? "Registrar dispositivo" : "Actualizar dispositivo",
                ModalityType.APPLICATION_MODAL);
        this.gestor = gestor;
        this.est = est;
        this.actual = actual;

        JPanel f = Estilo.tarjeta(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 10);

        fila(f, g, "Alias", alias);
        fila(f, g, "Tipo", tipo);
        g.gridx = 1;
        g.gridy++;
        g.gridwidth = 2;
        guia.setFont(Estilo.PEQUENA);
        f.add(guia, g);
        g.gridwidth = 1;
        soNoSe.setOpaque(false);
        fechaNoSe.setOpaque(false);
        fila(f, g, "Sistema operativo y versión", par(so, soNoSe));
        fila(f, g, "Última actualización (dd/mm/aaaa)", par(fecha, fechaNoSe));
        for (JCheckBox cb : new JCheckBox[] {antimalware, cifrado, bloqueo, compartido}) {
            cb.setOpaque(false);
            g.gridx = 1;
            g.gridy++;
            f.add(cb, g);
        }
        JPanel pw = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pw.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        for (FrecuenciaWifi fw : FrecuenciaWifi.values()) {
            JRadioButton r = new JRadioButton(fw.getValor() + " " + fw);
            r.setOpaque(false);
            wifi[fw.getValor() - 1] = r;
            bg.add(r);
            pw.add(r);
        }
        fila(f, g, "Uso de WiFi pública", pw);

        tipo.addActionListener(e -> actualizarGuia());
        soNoSe.addActionListener(e -> so.setEnabled(!soNoSe.isSelected()));
        fechaNoSe.addActionListener(e -> fecha.setEnabled(!fechaNoSe.isSelected()));

        if (actual != null) cargar(actual);
        actualizarGuia();

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancelar = Estilo.boton("Cancelar", false);
        JButton guardar = Estilo.boton("Guardar", true);
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar());
        botones.add(cancelar);
        botones.add(guardar);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        raiz.add(f, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(padre);
    }

    public Dispositivo abrir() {
        setVisible(true);
        return resultado;
    }

    private void actualizarGuia() {
        TipoDispositivo t = (TipoDispositivo) tipo.getSelectedItem();
        guia.setText("<html><div style='width:430px'>¿No sabes estos datos? " + Estilo.esc(t.getGuiaInformacion()) + "</div></html>");
        antimalware.setText("<html><div style='width:430px'>" + Estilo.esc(t.getPreguntaProteccion()) + "</div></html>");
        pack();
    }

    private void cargar(Dispositivo d) {
        alias.setText(d.getAlias());
        tipo.setSelectedItem(d.getTipoDispositivo());
        if (d.conoceSistemaOperativo()) so.setText(d.getSistemaOperativo());
        else {
            soNoSe.setSelected(true);
            so.setEnabled(false);
        }
        if (d.conoceFechaActualizacion()) fecha.setText(d.getFechaActualizacion().format(FECHA));
        else {
            fechaNoSe.setSelected(true);
            fecha.setEnabled(false);
        }
        antimalware.setSelected(d.tieneAntimalware());
        cifrado.setSelected(d.tieneCifrado());
        bloqueo.setSelected(d.tieneBloqueoPantalla());
        compartido.setSelected(d.esCompartido());
        wifi[d.getWifiPublica().getValor() - 1].setSelected(true);
    }

    private void guardar() {
        FrecuenciaWifi fw = null;
        for (int i = 0; i < wifi.length; i++) if (wifi[i].isSelected()) fw = FrecuenciaWifi.desdeEscala(i + 1);
        if (fw == null) {
            Estilo.error(this, "Indica con qué frecuencia usas WiFi pública (1 a 5).");
            return;
        }
        LocalDate f = null;
        if (!fechaNoSe.isSelected()) {
            try {
                f = LocalDate.parse(fecha.getText().trim(), FECHA);
            } catch (DateTimeParseException ex) {
                Estilo.error(this, "Fecha inválida. Usa el formato dd/mm/aaaa o marca «No sé».");
                return;
            }
        }
        String sistema = soNoSe.isSelected() ? Dispositivo.SO_DESCONOCIDO : so.getText();
        TipoDispositivo t = (TipoDispositivo) tipo.getSelectedItem();
        try {
            if (actual == null) {
                resultado = gestor.registrar(est, alias.getText(), t, sistema, f, antimalware.isSelected(),
                        cifrado.isSelected(), bloqueo.isSelected(), fw, compartido.isSelected());
            } else {
                resultado = gestor.actualizar(est, actual.getAlias(), alias.getText(), t, sistema, f, antimalware.isSelected(),
                        cifrado.isSelected(), bloqueo.isSelected(), fw, compartido.isSelected());
            }
            dispose();
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private static JPanel par(Component campo, Component extra) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.add(campo);
        p.add(extra);
        return p;
    }

    private static void fila(JPanel f, GridBagConstraints g, String etiqueta, Component campo) {
        g.gridy++;
        g.gridx = 0;
        JLabel l = new JLabel(etiqueta);
        l.setFont(Estilo.NEGRITA);
        f.add(l, g);
        g.gridx = 1;
        f.add(campo, g);
    }
}
