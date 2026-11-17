package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Dispositivo extends Evaluacion {
    private static final long serialVersionUID = 1L;
    public static final String SO_DESCONOCIDO = "No sé";
    public static final int PUNTOS_SO_DESCONOCIDO = 10;
    public static final int PUNTOS_FECHA_DESCONOCIDA = 20;

    private String alias;
    private TipoDispositivo tipo;
    private String sistemaOperativo;
    private LocalDate fechaActualizacion;
    private boolean antimalware;
    private boolean cifrado;
    private boolean bloqueoPantalla;
    private FrecuenciaWifi wifiPublica;
    private boolean compartido;

    public Dispositivo(String alias, TipoDispositivo tipo, String sistemaOperativo, LocalDate fechaActualizacion,
                       boolean antimalware, boolean cifrado, boolean bloqueoPantalla,
                       FrecuenciaWifi wifiPublica, boolean compartido) {
        super();
        actualizarDatos(alias, tipo, sistemaOperativo, fechaActualizacion, antimalware, cifrado,
                bloqueoPantalla, wifiPublica, compartido);
    }

    public final void actualizarDatos(String alias, TipoDispositivo tipo, String sistemaOperativo, LocalDate fechaActualizacion,
                                      boolean antimalware, boolean cifrado, boolean bloqueoPantalla,
                                      FrecuenciaWifi wifiPublica, boolean compartido) {
        this.alias = alias;
        this.tipo = tipo;
        this.sistemaOperativo = sistemaOperativo;
        this.fechaActualizacion = fechaActualizacion;
        this.antimalware = antimalware;
        this.cifrado = cifrado;
        this.bloqueoPantalla = bloqueoPantalla;
        this.wifiPublica = wifiPublica;
        this.compartido = compartido;
        actualizarFecha();
        evaluar();
    }

    public static boolean esSistemaSinSoporte(String sistemaOperativo) {
        if (esDesconocido(sistemaOperativo)) return false;
        String so = sistemaOperativo.toLowerCase(Locale.ROOT).trim();
        return so.matches(".*windows\\s*(xp|vista|7|8|8\\.1|10)\\b.*");
    }

    public static boolean esDesconocido(String sistemaOperativo) {
        return SO_DESCONOCIDO.equalsIgnoreCase(sistemaOperativo);
    }

    public boolean conoceSistemaOperativo() { return !esDesconocido(sistemaOperativo); }
    public boolean conoceFechaActualizacion() { return fechaActualizacion != null; }

    public long getDiasSinActualizar() {
        if (fechaActualizacion == null) return -1;
        return ChronoUnit.DAYS.between(fechaActualizacion, LocalDate.now());
    }

    private int factorActualizacion() {
        if (fechaActualizacion == null) return PUNTOS_FECHA_DESCONOCIDA;
        long d = getDiasSinActualizar();
        if (d <= 30) return 0;
        if (d <= 60) return 10;
        if (d <= 120) return 20;
        return 30;
    }

    private int factorAntimalware() {
        if (antimalware) return 0;
        return tipo.esComputadora() ? 15 : 10;
    }

    @Override
    public double calcularPuntaje() {
        int ie = factorActualizacion() + factorAntimalware() + (cifrado ? 0 : 15) + (bloqueoPantalla ? 0 : 15)
                + wifiPublica.getPuntos() + (compartido ? 15 : 0)
                + (conoceSistemaOperativo() ? 0 : PUNTOS_SO_DESCONOCIDO);
        ie = Math.min(100, ie);
        if (esSistemaSinSoporte(sistemaOperativo)) {
            ie = Math.max(ie, 75);
        }
        return ie;
    }

    @Override
    public String clasificar() {
        if (puntaje >= 75) return "Crítico";
        if (puntaje >= 50) return "Alto";
        if (puntaje >= 25) return "Medio";
        return "Bajo";
    }

    @Override
    public String getTipo() {
        return "Dispositivo";
    }

    @Override
    public List<String> getRecomendaciones() {
        List<String> lista = new ArrayList<>();
        if (esSistemaSinSoporte(sistemaOperativo)) {
            lista.add("Tu sistema operativo ya no recibe actualizaciones de seguridad. Actualiza a una versión con soporte.");
        }
        if (!conoceSistemaOperativo()) {
            lista.add("No conoces el sistema operativo: sin ese dato no se puede saber si aún recibe parches de seguridad. "
                    + "Búscalo así: " + tipo.getGuiaInformacion());
        }
        if (!conoceFechaActualizacion()) {
            String ruta = conoceSistemaOperativo() ? " (" + tipo.getGuiaInformacion() + ")" : " (en la misma ruta del punto anterior)";
            lista.add("No sabes cuándo se actualizó: revisa el historial de actualizaciones" + ruta
                    + " y activa las actualizaciones automáticas. Si aparece el aviso \"Actualizar\", acéptalo.");
        } else if (factorActualizacion() > 0) {
            lista.add("Lleva " + getDiasSinActualizar() + " días sin actualizarse. Instala las actualizaciones pendientes.");
        }
        if (!antimalware) lista.add(tipo.getRecomendacionProteccion());
        if (!cifrado) lista.add("Activa el cifrado del almacenamiento (BitLocker, FileVault o el cifrado del teléfono).");
        if (!bloqueoPantalla) lista.add("Activa el bloqueo de pantalla con PIN, contraseña o biometría.");
        if (wifiPublica.getPuntos() > 0) lista.add("Evita entrar a cuentas importantes desde WiFi públicas o usa una VPN.");
        if (compartido) lista.add(tipo.getRecomendacionCompartido());
        if (lista.isEmpty()) lista.add("Tu dispositivo está bien protegido. Mantén estas configuraciones.");
        return lista;
    }

    public boolean tieneAlerta() {
        return getDiasSinActualizar() > 60 || esSistemaSinSoporte(sistemaOperativo)
                || !conoceSistemaOperativo() || !conoceFechaActualizacion();
    }

    public String getAlias() { return alias; }
    public TipoDispositivo getTipoDispositivo() { return tipo; }
    public String getSistemaOperativo() { return sistemaOperativo; }
    public LocalDate getFechaActualizacion() { return fechaActualizacion; }
    public boolean tieneAntimalware() { return antimalware; }
    public boolean tieneCifrado() { return cifrado; }
    public boolean tieneBloqueoPantalla() { return bloqueoPantalla; }
    public FrecuenciaWifi getWifiPublica() { return wifiPublica; }
    public boolean esCompartido() { return compartido; }

    @Override
    public String toString() {
        return String.format("%-18s %-17s %-14s IE %3.0f/100 (%s)", alias, tipo, sistemaOperativo, puntaje, clasificacion);
    }
}
