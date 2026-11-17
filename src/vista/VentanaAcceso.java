package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import negocio.GestorUsuarios;
import negocio.ValidacionException;

public class VentanaAcceso extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final GestorUsuarios gu;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel derecha = new JPanel(tarjetas);
    private final JTextField correoLogin = Estilo.campo(new JTextField(22));

    public VentanaAcceso(ContextoAplicacion ctx) {
        super("Escudo Digital - UDLA");
        this.ctx = ctx;
        this.gu = ctx.getGestorUsuarios();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(panelMarca(), BorderLayout.WEST);
        derecha.setBackground(Estilo.FONDO);
        derecha.add(panelRegistro(), "registro");
        derecha.add(panelLogin(), "login");
        raiz.add(derecha, BorderLayout.CENTER);
        setContentPane(raiz);
        tarjetas.show(derecha, gu.existeCuenta() ? "login" : "registro");
        if (!gu.existeCuenta()) {
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowOpened(java.awt.event.WindowEvent e) {
                    abrirAviso();
                }
            });
        }
        setSize(1000, 680);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
    }

    private JPanel panelMarca() {
        JPanel p = new PanelDegradado(new BorderLayout(), false);
        p.setPreferredSize(new Dimension(440, 10));
        p.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        JLabel logo = new JLabel("Escudo Digital", new Icono(Icono.Tipo.ESCUDO, 34, Color.WHITE), SwingConstants.LEFT);
        logo.setIconTextGap(12);
        logo.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        p.add(logo, BorderLayout.NORTH);
        p.add(new Ilustracion(), BorderLayout.CENTER);
        JLabel lema = new JLabel("<html><div style='width:360px'><b>Tu seguridad digital, en un solo lugar.</b><br>"
                + "Diagnostica tus hábitos, revisa tus contraseñas y aprende a reconocer el phishing.</div></html>");
        lema.setFont(Estilo.NORMAL);
        lema.setForeground(new Color(255, 255, 255, 220));
        p.add(lema, BorderLayout.SOUTH);
        return p;
    }

    private JPanel panelRegistro() {
        JPanel f = formulario();
        GridBagConstraints g = base();
        f.add(saludo(), g);
        g.gridy++;
        f.add(titulo("Crea tu cuenta"), g);
        g.gridy++;
        g.insets = new Insets(0, 0, 10, 0);
        f.add(Estilo.ayudaAncho("Solo se permite una cuenta por equipo. La contraseña la genera el sistema y no se puede cambiar.", 320), g);

        JTextField nombres = Estilo.campo(new JTextField());
        JTextField correo = Estilo.campo(new JTextField());
        JTextField carrera = Estilo.campo(new JTextField());
        JComboBox<Integer> semestre = new JComboBox<>();
        for (int i = GestorUsuarios.SEMESTRE_MIN; i <= GestorUsuarios.SEMESTRE_MAX; i++) semestre.addItem(i);
        semestre.setPrototypeDisplayValue(12);
        fila(f, g, Icono.Tipo.PERSONA, "Nombres y apellidos", nombres);
        fila(f, g, Icono.Tipo.CORREO, "Correo institucional (@udla.edu.ec)", correo);
        fila(f, g, Icono.Tipo.LIBRO, "Carrera", carrera);
        JPanel sem = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sem.setOpaque(false);
        sem.add(semestre);
        fila(f, g, Icono.Tipo.BIRRETE, "Semestre (1 a 12)", sem);

        g.gridy++;
        g.insets = new Insets(14, 0, 0, 0);
        JCheckBox acepta = new JCheckBox("He leído y acepto el aviso de privacidad");
        acepta.setOpaque(false);
        acepta.setForeground(Estilo.TEXTO);
        f.add(acepta, g);

        g.gridy++;
        g.insets = new Insets(14, 0, 0, 0);
        JButton crear = Estilo.boton("Crear mi cuenta", true);
        f.add(crear, g);
        crear.addActionListener(e -> {
            try {
                String clave = gu.registrar(nombres.getText(), correo.getText(), carrera.getText(),
                        (Integer) semestre.getSelectedItem(), acepta.isSelected());
                ctx.guardar();
                mostrarContrasena(clave);
                correoLogin.setText(correo.getText().trim().toLowerCase());
                tarjetas.show(derecha, "login");
            } catch (ValidacionException ex) {
                Estilo.error(this, ex.getMessage());
            }
        });
        return envolver(f, botonAviso());
    }

    private JPanel botonAviso() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        JButton b = Estilo.boton("Aviso de privacidad", false);
        b.addActionListener(e -> abrirAviso());
        p.add(b);
        return p;
    }

    private void abrirAviso() {
        StringBuilder sb = new StringBuilder(Estilo.h("Cómo protegemos tus datos"));
        sb.append("<ul>");
        for (String l : GestorUsuarios.AVISO_PRIVACIDAD) sb.append("<li style='margin-bottom:6px'>").append(Estilo.esc(l)).append("</li>");
        sb.append("</ul>");
        Estilo.mostrarHtml(this, "Aviso de privacidad", sb.toString());
    }

    private void mostrarContrasena(String clave) {
        JDialog d = new JDialog(this, "Tu contraseña", true);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Estilo.SUPERFICIE);
        p.setBorder(BorderFactory.createEmptyBorder(22, 26, 20, 26));
        GridBagConstraints g = base();
        p.add(titulo("Cuenta creada"), g);
        g.gridy++;
        p.add(Estilo.ayudaAncho("Esta es tu contraseña. <b>Anótala ahora</b>: no se volverá a mostrar y no se puede cambiar.", 340), g);
        g.gridy++;
        JTextField campo = new JTextField(clave);
        campo.setEditable(false);
        campo.setHorizontalAlignment(SwingConstants.CENTER);
        campo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        campo.setBackground(Estilo.SUPERFICIE_ALTA);
        campo.setForeground(Estilo.ACENTO_CLARO);
        campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.ACENTO),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        g.insets = new Insets(14, 0, 16, 0);
        p.add(campo, g);
        g.gridy++;
        g.insets = new Insets(0, 0, 0, 0);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        JButton copiar = Estilo.boton("Copiar", false);
        JButton listo = Estilo.boton("Ya la anoté", true);
        copiar.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(clave), null);
            copiar.setText("Copiada");
        });
        listo.addActionListener(e -> d.dispose());
        botones.add(copiar);
        botones.add(listo);
        p.add(botones, g);
        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private JPanel panelLogin() {
        JPanel f = formulario();
        GridBagConstraints g = base();
        f.add(saludo(), g);
        g.gridy++;
        f.add(titulo("Inicia sesión"), g);
        g.gridy++;
        g.insets = new Insets(0, 0, 14, 0);
        f.add(Estilo.ayudaAncho("Ingresa con tu correo institucional y la contraseña que te dio el sistema.", 320), g);
        JPasswordField clave = Estilo.campo(new JPasswordField());
        fila(f, g, Icono.Tipo.CORREO, "Correo institucional", correoLogin);
        fila(f, g, Icono.Tipo.CANDADO, "Contraseña", clave);
        g.gridy++;
        g.insets = new Insets(8, 32, 0, 0);
        JCheckBox ver = new JCheckBox("Mostrar contraseña");
        ver.setOpaque(false);
        ver.setForeground(Estilo.TEXTO_SUAVE);
        char eco = clave.getEchoChar();
        ver.addActionListener(e -> clave.setEchoChar(ver.isSelected() ? (char) 0 : eco));
        f.add(ver, g);
        g.gridy++;
        g.insets = new Insets(22, 0, 0, 0);
        JButton entrar = Estilo.boton("Ingresar", true);
        f.add(entrar, g);
        Runnable accion = () -> {
            char[] c = clave.getPassword();
            try {
                gu.iniciarSesion(correoLogin.getText(), c);
                clave.setText("");
                ctx.guardar();
                dispose();
                new VentanaPrincipal(ctx).setVisible(true);
            } catch (ValidacionException ex) {
                ctx.guardar();
                Estilo.error(this, ex.getMessage());
            } finally {
                Arrays.fill(c, '\0');
            }
        };
        entrar.addActionListener(e -> accion.run());
        clave.addActionListener(e -> accion.run());
        return envolver(f, botonAviso());
    }

    private static JLabel saludo() {
        JLabel l = new JLabel("Te ayudamos a protegerte");
        l.setFont(new Font(Estilo.FAMILIA, Font.ITALIC, 13));
        l.setForeground(Estilo.MAGENTA_CLARO);
        return l;
    }

    private static JLabel titulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 26));
        l.setForeground(Estilo.TEXTO);
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));
        return l;
    }

    private static JPanel formulario() {
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        return f;
    }

    private static GridBagConstraints base() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 4, 0);
        return g;
    }

    private static void fila(JPanel p, GridBagConstraints g, Icono.Tipo icono, String etiqueta, JComponent campo) {
        g.gridy++;
        g.insets = new Insets(10, 32, 2, 0);
        JLabel l = new JLabel(etiqueta);
        l.setFont(Estilo.PEQUENA);
        l.setForeground(Estilo.TEXTO_SUAVE);
        p.add(l, g);
        g.gridy++;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(Estilo.conIcono(icono, campo), g);
    }

    private static JPanel envolver(JPanel formulario, JPanel abajo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Estilo.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(26, 60, 22, 40));
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.weightx = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.CENTER;
        formulario.setPreferredSize(null);
        JPanel limite = new JPanel(new BorderLayout());
        limite.setOpaque(false);
        limite.setPreferredSize(new Dimension(420, formulario.getPreferredSize().height));
        limite.add(formulario, BorderLayout.CENTER);
        centro.add(limite, g);
        p.add(centro, BorderLayout.CENTER);
        p.add(abajo, BorderLayout.SOUTH);
        return p;
    }
}
