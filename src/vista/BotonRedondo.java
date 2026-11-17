package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class BotonRedondo extends JButton {
    private static final long serialVersionUID = 1L;

    public enum Tipo { PRINCIPAL, SECUNDARIO, PELIGRO }

    private final Tipo tipo;
    private boolean encima;

    public BotonRedondo(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setFont(Estilo.NEGRITA);
        setForeground(tipo == Tipo.SECUNDARIO ? Estilo.ACENTO_CLARO : Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { encima = false; repaint(); }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 38));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), r = h;
        float alfa = isEnabled() ? 1f : 0.4f;
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alfa));
        if (tipo == Tipo.PRINCIPAL) {
            Color a = encima ? Estilo.MAGENTA.brighter() : Estilo.MAGENTA;
            Color b = encima ? Estilo.MORADO.brighter() : Estilo.MORADO;
            g2.setPaint(new GradientPaint(0, 0, a, w, h, b));
            g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
        } else if (tipo == Tipo.PELIGRO) {
            g2.setColor(encima ? Estilo.ROJO.brighter() : Estilo.ROJO_FUERTE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
        } else {
            g2.setColor(encima ? Estilo.SUPERFICIE_ALTA : Estilo.SUPERFICIE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);
            g2.setColor(Estilo.ACENTO);
            g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
