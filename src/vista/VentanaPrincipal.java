package vista;

import interfaz.ContextoAplicacion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import modelo.Estudiante;
import negocio.ValidacionException;

public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<String, Component> paneles = new LinkedHashMap<>();
    private final Map<String, JButton> botones = new LinkedHashMap<>();
    private final JLabel nombreUsuario = new JLabel();

    public VentanaPrincipal(ContextoAplicacion ctx) throws ValidacionException {
        super("Escudo Digital - UDLA");
        this.ctx = ctx;
        this.est = ctx.getGestorUsuarios().getEstudianteActivo();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ctx.guardar();
                System.exit(0);
            }
        });

        agregar("inicio", new PanelInicio(ctx, est, this));
        agregar("diagnostico", new PanelDiagnostico(ctx, est));
        agregar("credenciales", new PanelCredenciales(ctx, est));
        agregar("dispositivos", new PanelDispositivos(ctx, est));
        agregar("phishing", new PanelPhishing(ctx, est));
        agregar("entrenamiento", new PanelEntrenamiento(ctx, est));
        agregar("riesgo", new PanelRiesgo(ctx, est));
        agregar("perfil", new PanelPerfil(ctx, est, this));

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(menuLateral(), BorderLayout.WEST);
        contenido.setBackground(Estilo.FONDO);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        mostrar("inicio");
    }

    private void agregar(String clave, Component panel) {
        paneles.put(clave, panel);
        contenido.add(panel, clave);
    }

    public void mostrar(String clave) {
        Component p = paneles.get(clave);
        if (p instanceof Actualizable) ((Actualizable) p).actualizar();
        tarjetas.show(contenido, clave);
        for (Map.Entry<String, JButton> e : botones.entrySet()) marcar(e.getValue(), e.getKey().equals(clave));
        nombreUsuario.setText("<html><b>" + Estilo.esc(est.getNombres()) + "</b><br>" + Estilo.esc(est.getCorreo()) + "</html>");
    }

    private JPanel menuLateral() {
        JPanel m = new PanelDegradado(null, true);
        m.setLayout(new BoxLayout(m, BoxLayout.Y_AXIS));
        m.setPreferredSize(new Dimension(290, 10));
        m.setBorder(BorderFactory.createEmptyBorder(20, 12, 16, 12));

        JLabel marca = new JLabel("Escudo Digital", new Icono(Icono.Tipo.ESCUDO, 30, Color.WHITE), SwingConstants.LEFT);
        marca.setIconTextGap(10);
        marca.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 20));
        marca.setForeground(Color.WHITE);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        m.add(marca);
        m.add(Box.createVerticalStrut(14));
        nombreUsuario.setForeground(new Color(255, 255, 255, 190));
        nombreUsuario.setFont(Estilo.PEQUENA);
        nombreUsuario.setIcon(new Icono(Icono.Tipo.PERSONA, 20, new Color(255, 255, 255, 190)));
        nombreUsuario.setIconTextGap(8);
        nombreUsuario.setBorder(BorderFactory.createEmptyBorder(0, 8, 12, 8));
        nombreUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        m.add(nombreUsuario);

        m.add(item("inicio", "Inicio"));
        m.add(grupo("MÓDULO 1 · ACCESO Y DIAGNÓSTICO"));
        m.add(item("diagnostico", "Diagnóstico de higiene digital"));
        m.add(grupo("MÓDULO 2 · CREDENCIALES"));
        m.add(item("credenciales", "Contraseñas y correos filtrados"));
        m.add(item("dispositivos", "Mis dispositivos"));
        m.add(grupo("MÓDULO 3 · INGENIERÍA SOCIAL"));
        m.add(item("phishing", "Analizar un mensaje sospechoso"));
        m.add(item("entrenamiento", "Entrenamiento antiphishing"));
        m.add(item("riesgo", "Índice global de riesgo"));
        m.add(grupo("MI CUENTA"));
        m.add(item("perfil", "Mi perfil"));
        m.add(Box.createVerticalGlue());

        ItemMenu limpiar = new ItemMenu("Limpiar mi historial");
        limpiar.addActionListener(e -> limpiarHistorial());
        m.add(limpiar);
        ItemMenu salir = new ItemMenu("Cerrar sesión");
        salir.addActionListener(e -> cerrarSesion());
        m.add(salir);
        return m;
    }

    private JLabel grupo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 11));
        l.setForeground(new Color(255, 255, 255, 140));
        l.setBorder(BorderFactory.createEmptyBorder(14, 10, 4, 8));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private ItemMenu item(String clave, String texto) {
        ItemMenu b = new ItemMenu(texto);
        b.addActionListener(e -> mostrar(clave));
        botones.put(clave, b);
        return b;
    }

    private void marcar(JButton b, boolean activo) {
        ((ItemMenu) b).activo = activo;
        b.setFont(activo ? Estilo.NEGRITA : Estilo.NORMAL);
        b.repaint();
    }

    private static class ItemMenu extends JButton {
        private static final long serialVersionUID = 1L;
        boolean activo;
        private boolean encima;

        ItemMenu(String texto) {
            super(texto);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(Estilo.NORMAL);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 10));
            setMaximumSize(new Dimension(280, 40));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { encima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            if (activo || encima) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, activo ? 48 : 22));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                if (activo) {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(2, 9, 4, getHeight() - 18, 4, 4);
                }
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private void limpiarHistorial() {
        if (!Estilo.confirmar(this, "Se borrarán los resultados de todos los módulos. Tu cuenta se mantiene.\n¿Deseas continuar?")) return;
        try {
            int n = ctx.getGestorUsuarios().limpiarHistorial();
            ctx.guardar();
            Estilo.info(this, "Historial limpio. Se borraron " + n + " resultados.");
            mostrar("inicio");
        } catch (ValidacionException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    public void cerrarSesion() {
        ctx.getGestorUsuarios().cerrarSesion();
        ctx.guardar();
        dispose();
        new VentanaAcceso(ctx).setVisible(true);
    }
}
