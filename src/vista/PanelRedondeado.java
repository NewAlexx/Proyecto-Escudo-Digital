package vista;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PanelRedondeado extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int RADIO = 18;

    private Color acento;
    private Color borde = Estilo.BORDE;

    public PanelRedondeado(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
    }

    public PanelRedondeado conAcento(Color c) {
        this.acento = c;
        setBorder(BorderFactory.createEmptyBorder(20, 18, 16, 18));
        return this;
    }

    public PanelRedondeado conBorde(Color c) {
        this.borde = c;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 1, h = getHeight() - 1;
        g2.setColor(Estilo.SUPERFICIE);
        g2.fillRoundRect(0, 0, w, h, RADIO, RADIO);
        if (acento != null) {
            g2.setClip(0, 0, w + 1, 6);
            g2.setColor(acento);
            g2.fillRoundRect(0, 0, w, h, RADIO, RADIO);
            g2.setClip(null);
        }
        g2.setColor(borde);
        g2.drawRoundRect(0, 0, w, h, RADIO, RADIO);
        g2.dispose();
        super.paintComponent(g);
    }
}
