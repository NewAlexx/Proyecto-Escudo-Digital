package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

public class Ilustracion extends JComponent {
    private static final long serialVersionUID = 1L;

    public Ilustracion() {
        setPreferredSize(new Dimension(380, 300));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        double esc = Math.min(w / 380.0, h / 300.0);
        g2.translate((w - 380 * esc) / 2, (h - 300 * esc) / 2);
        g2.scale(esc, esc);

        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillOval(250, 0, 130, 130);
        g2.fillOval(-10, 200, 100, 100);
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillOval(300, 230, 40, 40);

        g2.setColor(new Color(20, 16, 34, 230));
        g2.fillRoundRect(30, 40, 300, 200, 18, 18);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(30, 40, 300, 28, 18, 18);
        g2.fillRect(30, 56, 300, 12);
        int[] puntos = {48, 62, 76};
        Color[] cp = {new Color(0xFF5C6C), new Color(0xFFB14E), new Color(0x4CC38A)};
        for (int i = 0; i < 3; i++) {
            g2.setColor(cp[i]);
            g2.fillOval(puntos[i] - 5, 49, 10, 10);
        }

        Path2D p = new Path2D.Double();
        p.moveTo(110, 82);
        p.lineTo(160, 100);
        p.curveTo(160, 150, 140, 185, 110, 205);
        p.curveTo(80, 185, 60, 150, 60, 100);
        p.closePath();
        g2.setPaint(new GradientPaint(60, 82, Estilo.MAGENTA, 160, 205, Estilo.MORADO));
        g2.fill(p);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(p);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(97, 115, 26, 30, 0, 180);
        g2.fillRoundRect(91, 130, 38, 32, 8, 8);
        g2.setColor(Estilo.MORADO_OSCURO);
        g2.fillOval(106, 140, 8, 8);
        g2.fillRect(108, 146, 4, 9);

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(180, 110, 130, 30, 15, 15);
        g2.setColor(Estilo.MORADO_OSCURO);
        for (int i = 0; i < 6; i++) g2.fillOval(194 + i * 17, 120, 10, 10);
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(180, 152, 130, 8, 8, 8);
        g2.setColor(new Color(0x4CC38A));
        g2.fillRoundRect(180, 152, 100, 8, 8, 8);
        g2.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 12));
        g2.setColor(new Color(255, 255, 255, 210));
        g2.drawString("Contraseña fuerte", 182, 178);

        g2.setColor(new Color(255, 255, 255, 235));
        g2.fillRoundRect(205, 192, 70, 46, 8, 8);
        g2.setColor(Estilo.MORADO_OSCURO);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(new int[] {205, 240, 275}, new int[] {194, 218, 194}, 3);
        g2.setColor(new Color(0xFF5C6C));
        g2.fillOval(262, 180, 26, 26);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Estilo.FAMILIA, Font.BOLD, 18));
        g2.drawString("!", 272, 200);
        g2.dispose();
    }
}
