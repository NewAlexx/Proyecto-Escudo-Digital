package negocio;

public interface ServicioContrasenasFiltradas {
    int contarApariciones(String sha1Hex) throws ServicioNoDisponibleException;
}
