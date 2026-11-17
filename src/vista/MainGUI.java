package vista;

import interfaz.ContextoAplicacion;
import javax.swing.SwingUtilities;

public class MainGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarTema();
            ContextoAplicacion contexto = new ContextoAplicacion();
            Runtime.getRuntime().addShutdownHook(new Thread(contexto::guardar));
            new VentanaAcceso(contexto).setVisible(true);
        });
    }
}
