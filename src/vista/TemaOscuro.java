package vista;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class TemaOscuro extends DefaultMetalTheme {
    private static ColorUIResource c(java.awt.Color x) { return new ColorUIResource(x); }

    @Override public String getName() { return "Escudo Digital oscuro"; }
    @Override protected ColorUIResource getPrimary1() { return c(Estilo.ACENTO); }
    @Override protected ColorUIResource getPrimary2() { return c(Estilo.ACENTO_OSCURO); }
    @Override protected ColorUIResource getPrimary3() { return c(Estilo.SUPERFICIE_ALTA); }
    @Override protected ColorUIResource getSecondary1() { return c(Estilo.BORDE); }
    @Override protected ColorUIResource getSecondary2() { return c(Estilo.BORDE); }
    @Override protected ColorUIResource getSecondary3() { return c(Estilo.FONDO); }
    @Override protected ColorUIResource getBlack() { return c(Estilo.TEXTO); }
    @Override protected ColorUIResource getWhite() { return c(Estilo.SUPERFICIE_ALTA); }
    @Override public ColorUIResource getWindowBackground() { return c(Estilo.SUPERFICIE_ALTA); }
    @Override public ColorUIResource getControlTextColor() { return c(Estilo.TEXTO); }
    @Override public ColorUIResource getSystemTextColor() { return c(Estilo.TEXTO); }
    @Override public ColorUIResource getUserTextColor() { return c(Estilo.TEXTO); }
    @Override public ColorUIResource getInactiveControlTextColor() { return c(Estilo.TEXTO_SUAVE); }
    @Override public ColorUIResource getMenuForeground() { return c(Estilo.TEXTO); }
    @Override public ColorUIResource getHighlightedTextColor() { return c(Estilo.TEXTO); }
    @Override public ColorUIResource getTextHighlightColor() { return c(Estilo.ACENTO_OSCURO); }
}
