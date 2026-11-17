package vista;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.JPanel;

public class PanelDegradado extends JPanel {
    private static final long serialVersionUID = 1L;
    private final boolean vertical;

    public PanelDegradado(LayoutManager layout, boolean vertical) {
        super(layout);
        this.vertical = vertical;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setPaint(vertical
                ? new GradientPaint(0, 0, Estilo.MAGENTA_OSCURO, 0, h, Estilo.MORADO_OSCURO)
                : new GradientPaint(0, 0, Estilo.MAGENTA, w, h, Estilo.MORADO));
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}
