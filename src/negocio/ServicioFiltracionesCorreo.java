package negocio;

import java.util.List;

public interface ServicioFiltracionesCorreo {
    List<String> buscarFiltraciones(String correo) throws ServicioNoDisponibleException;

    String getNombre();
}
