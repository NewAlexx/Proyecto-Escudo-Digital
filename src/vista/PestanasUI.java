package vista;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class PestanasUI extends BasicTabbedPaneUI {
    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent c) {
        return new PestanasUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabAreaInsets = new Insets(0, 0, 0, 0);
        contentBorderInsets = new Insets(1, 0, 0, 0);
        tabInsets = new Insets(9, 16, 9, 16);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabPane.setOpaque(false);
        tabPane.setForeground(Estilo.TEXTO_SUAVE);
    }

    @Override
    protected void paintTabBackground(Graphics g, int placement, int index, int x, int y, int w, int h, boolean sel) {
        g.setColor(sel ? Estilo.SUPERFICIE : Estilo.FONDO);
        g.fillRect(x, y, w, h);
    }

    @Override
    protected void paintTabBorder(Graphics g, int placement, int index, int x, int y, int w, int h, boolean sel) {
        if (sel) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.MAGENTA_CLARO);
            g2.fillRoundRect(x + 6, y + h - 3, w - 12, 3, 3, 3);
        }
    }

    @Override
    protected void paintText(Graphics g, int placement, java.awt.Font font, FontMetrics fm, int index, String title,
                             Rectangle textRect, boolean sel) {
        g.setFont(sel ? Estilo.NEGRITA : font);
        g.setColor(sel ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(title, textRect.x, textRect.y + fm.getAscent());
    }

    @Override
    protected void paintContentBorder(Graphics g, int placement, int selectedIndex) {
        Rectangle r = tabPane.getBounds();
        int y = calculateTabAreaHeight(placement, runCount, maxTabHeight);
        g.setColor(Estilo.BORDE);
        g.drawLine(0, y, r.width, y);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int placement, Rectangle[] rects, int index, Rectangle iconRect,
                                       Rectangle textRect, boolean sel) {
    }

    @Override
    protected int calculateTabWidth(int placement, int index, FontMetrics metrics) {
        return super.calculateTabWidth(placement, index, tabPane.getFontMetrics(Estilo.NEGRITA));
    }
}
