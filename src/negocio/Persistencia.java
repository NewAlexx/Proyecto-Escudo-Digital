package negocio;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Persistencia {
    private static final Path RUTA = Path.of("escudo.dat");

    private Persistencia() { }

    public static void guardar(GestorUsuarios gestor) {
        try {
            if (!gestor.existeCuenta()) {
                Files.deleteIfExists(RUTA);
                return;
            }
            try (OutputStream os = Files.newOutputStream(RUTA); ObjectOutputStream out = new ObjectOutputStream(os)) {
                out.writeObject(gestor);
            }
        } catch (IOException e) {
            System.err.println("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    public static GestorUsuarios cargar() {
        if (!Files.exists(RUTA)) {
            return new GestorUsuarios();
        }
        try (InputStream is = Files.newInputStream(RUTA); ObjectInputStream in = new ObjectInputStream(is)) {
            return (GestorUsuarios) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("No se pudieron leer los datos guardados; se inicia con datos vacíos. (" + e.getMessage() + ")");
            return new GestorUsuarios();
        }
    }
}
