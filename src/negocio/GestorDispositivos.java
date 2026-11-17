package negocio;

import java.time.LocalDate;
import java.util.List;
import modelo.Dispositivo;
import modelo.Estudiante;
import modelo.FrecuenciaWifi;
import modelo.TipoDispositivo;

public class GestorDispositivos {
    public static final int MAX_DISPOSITIVOS = 5;

    public Dispositivo registrar(Estudiante estudiante, String alias, TipoDispositivo tipo, String sistemaOperativo,
                                 LocalDate fechaActualizacion, boolean antimalware, boolean cifrado, boolean bloqueo,
                                 FrecuenciaWifi wifi, boolean compartido) throws ValidacionException {
        if (listar(estudiante).size() >= MAX_DISPOSITIVOS) {
            throw new ValidacionException("Alcanzaste el máximo de " + MAX_DISPOSITIVOS + " dispositivos.");
        }
        validar(estudiante, null, alias, tipo, sistemaOperativo, fechaActualizacion, wifi);
        Dispositivo d = new Dispositivo(alias.trim(), tipo, normalizarSO(sistemaOperativo), fechaActualizacion,
                antimalware, cifrado, bloqueo, wifi, compartido);
        estudiante.agregarEvaluacion(d);
        return d;
    }

    public List<Dispositivo> listar(Estudiante estudiante) {
        return estudiante.getEvaluaciones(Dispositivo.class);
    }

    public Dispositivo buscar(Estudiante estudiante, String alias) throws ValidacionException {
        for (Dispositivo d : listar(estudiante)) {
            if (d.getAlias().equalsIgnoreCase(alias.trim())) return d;
        }
        throw new ValidacionException("No tienes un dispositivo llamado '" + alias + "'.");
    }

    public Dispositivo actualizar(Estudiante estudiante, String aliasActual, String nuevoAlias, TipoDispositivo tipo,
                                  String sistemaOperativo, LocalDate fechaActualizacion, boolean antimalware,
                                  boolean cifrado, boolean bloqueo, FrecuenciaWifi wifi, boolean compartido)
            throws ValidacionException {
        Dispositivo d = buscar(estudiante, aliasActual);
        validar(estudiante, d, nuevoAlias, tipo, sistemaOperativo, fechaActualizacion, wifi);
        d.actualizarDatos(nuevoAlias.trim(), tipo, normalizarSO(sistemaOperativo), fechaActualizacion,
                antimalware, cifrado, bloqueo, wifi, compartido);
        return d;
    }

    public void eliminar(Estudiante estudiante, String alias) throws ValidacionException {
        estudiante.eliminarEvaluacion(buscar(estudiante, alias));
    }

    static String normalizarSO(String so) {
        String t = java.text.Normalizer.normalize(so.trim().toLowerCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace(" ", "");
        return t.equals("nose") ? Dispositivo.SO_DESCONOCIDO : so.trim();
    }

    private void validar(Estudiante estudiante, Dispositivo actual, String alias, TipoDispositivo tipo, String so,
                         LocalDate fecha, FrecuenciaWifi wifi) throws ValidacionException {
        if (alias == null || alias.isBlank() || so == null || so.isBlank() || tipo == null || wifi == null) {
            throw new ValidacionException("Todos los campos son obligatorios.");
        }
        for (Dispositivo d : listar(estudiante)) {
            if (d != actual && d.getAlias().equalsIgnoreCase(alias.trim())) {
                throw new ValidacionException("Ya tienes un dispositivo llamado '" + alias.trim() + "'.");
            }
        }
        if (fecha != null && fecha.isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de actualización no puede ser posterior a hoy.");
        }
    }
}
