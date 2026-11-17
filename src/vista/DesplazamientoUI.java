package vista;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class DesplazamientoUI extends BasicScrollBarUI {
    public static javax.swing.plaf.ComponentUI createUI(JComponent c) {
        return new DesplazamientoUI();
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(Estilo.SUPERFICIE);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (r.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isThumbRollover() ? Estilo.TEXTO_SUAVE : Estilo.BORDE_FUERTE);
        g2.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, 8, 8);
        g2.dispose();
    }

    @Override protected JButton createDecreaseButton(int o) { return sinBoton(); }
    @Override protected JButton createIncreaseButton(int o) { return sinBoton(); }

    private static JButton sinBoton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }
}
