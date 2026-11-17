package negocio;

public interface ServicioReputacion {
    int analizarUrl(String url) throws ServicioNoDisponibleException;
}
