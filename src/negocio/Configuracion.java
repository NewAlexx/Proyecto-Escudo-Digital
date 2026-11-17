package negocio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Configuracion {
    private static final Path ARCHIVO = Path.of("config.properties");
    private static final Properties PROPIEDADES = new Properties();

    static {
        if (Files.exists(ARCHIVO)) {
            try (InputStream in = Files.newInputStream(ARCHIVO)) {
                PROPIEDADES.load(in);
            } catch (IOException e) {
                System.err.println("No se pudo leer config.properties: " + e.getMessage());
            }
        }
    }

    private Configuracion() { }

    public static String obtener(String clave) {
        String valor = PROPIEDADES.getProperty(clave, "").trim();
        return valor.isEmpty() ? null : valor;
    }
}
