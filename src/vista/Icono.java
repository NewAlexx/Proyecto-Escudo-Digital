package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

public class Icono implements Icon {
    public enum Tipo { ESCUDO, PERSONA, CORREO, CANDADO, LIBRO, BIRRETE }

    private final Tipo tipo;
    private final int t;
    private final Color color;

    public Icono(Tipo tipo, int tamano, Color color) {
        this.tipo = tipo;
        this.t = tamano;
        this.color = color;
    }

    @Override public int getIconWidth() { return t; }
    @Override public int getIconHeight() { return t; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(color);
        float s = t / 24f;
        g2.scale(s, s);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (tipo) {
            case ESCUDO: escudo(g2); break;
            case PERSONA:
                g2.fillOval(8, 3, 8, 8);
                g2.fillRoundRect(4, 13, 16, 9, 8, 8);
                break;
            case CORREO:
                g2.drawRoundRect(3, 6, 18, 13, 3, 3);
                g2.drawPolyline(new int[] {3, 12, 21}, new int[] {7, 14, 7}, 3);
                break;
            case CANDADO:
                g2.drawArc(7, 3, 10, 12, 0, 180);
                g2.drawLine(7, 9, 7, 11);
                g2.drawLine(17, 9, 17, 11);
                g2.fillRoundRect(4, 11, 16, 11, 4, 4);
                break;
            case LIBRO:
                g2.drawRoundRect(4, 4, 16, 17, 3, 3);
                g2.drawLine(8, 4, 8, 21);
                g2.drawLine(11, 9, 17, 9);
                g2.drawLine(11, 13, 17, 13);
                break;
            case BIRRETE:
                g2.fillPolygon(new int[] {2, 12, 22, 12}, new int[] {9, 4, 9, 14}, 4);
                g2.drawArc(6, 8, 12, 9, 180, 180);
                g2.drawLine(20, 9, 20, 16);
                break;
            default: break;
        }
        g2.dispose();
    }

    private static void escudo(Graphics2D g2) {
        Path2D p = new Path2D.Float();
        p.moveTo(12, 1.5);
        p.lineTo(21, 5);
        p.curveTo(21, 13, 17.5, 19, 12, 22.5);
        p.curveTo(6.5, 19, 3, 13, 3, 5);
        p.closePath();
        g2.fill(p);
        Color c = g2.getColor();
        g2.setColor(new Color(0, 0, 0, 90));
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawPolyline(new int[] {8, 11, 16}, new int[] {12, 15, 9}, 3);
        g2.setColor(c);
    }
}
