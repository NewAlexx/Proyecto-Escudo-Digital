package interfaz;

import java.time.LocalDate;
import java.util.List;
import modelo.Dispositivo;
import modelo.Estudiante;
import modelo.FrecuenciaWifi;
import modelo.TipoDispositivo;
import negocio.GestorDispositivos;
import negocio.ValidacionException;

public class MenuDispositivos {
    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorDispositivos gestor;

    public MenuDispositivos(ContextoAplicacion ctx, Estudiante est) {
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorDispositivos();
    }

    public void mostrar() {
        while (true) {
            Consola.titulo("MIS DISPOSITIVOS");
            Consola.linea("1. Registrar dispositivo");
            Consola.linea("2. Listar dispositivos");
            Consola.linea("3. Ver detalle y recomendaciones");
            Consola.linea("4. Actualizar dispositivo");
            Consola.linea("5. Eliminar dispositivo");
            Consola.linea("0. Volver");
            int op = Consola.leerOpcion(5);
            if (op == 0) return;
            try {
                switch (op) {
                    case 1: registrar(); break;
                    case 2: listar(); break;
                    case 3: mostrarDetalle(gestor.buscar(est, Consola.leerTexto("Alias"))); break;
                    case 4: actualizar(); break;
                    case 5: gestor.eliminar(est, Consola.leerTexto("Alias")); Consola.exito("Dispositivo eliminado."); break;
                    default: break;
                }
            } catch (ValidacionException e) {
                Consola.error(e.getMessage());
            }
        }
    }

    private void registrar() throws ValidacionException {
        if (gestor.listar(est).size() >= GestorDispositivos.MAX_DISPOSITIVOS) {
            throw new ValidacionException("Alcanzaste el máximo de " + GestorDispositivos.MAX_DISPOSITIVOS + " dispositivos.");
        }
        String alias = Consola.leerTexto("Alias (por ejemplo, Laptop personal)");
        TipoDispositivo tipo = Consola.leerEnum("Tipo", TipoDispositivo.values());
        String so = leerSistemaOperativo(tipo, "Sistema operativo y versión (por ejemplo, Windows 11)");
        LocalDate fecha = leerFechaActualizacion(tipo);
        boolean antimalware = Consola.leerSiNo(tipo.getPreguntaProteccion());
        boolean cifrado = Consola.leerSiNo("¿Tiene el almacenamiento cifrado?");
        boolean bloqueo = Consola.leerSiNo("¿Tiene bloqueo de pantalla?");
        FrecuenciaWifi wifi = leerUsoWifi();
        boolean compartido = Consola.leerSiNo("¿Lo comparte con otras personas?");
        Dispositivo d = gestor.registrar(est, alias, tipo, so, fecha, antimalware, cifrado, bloqueo, wifi, compartido);
        Consola.exito(String.format("Dispositivo '%s' registrado. Índice de exposición: %.0f/100 (%s).", d.getAlias(), d.getPuntaje(), d.getClasificacion()));
        mostrarDetalle(d);
    }

    private void actualizar() throws ValidacionException {
        Dispositivo d = gestor.buscar(est, Consola.leerTexto("Alias del dispositivo a actualizar"));
        Consola.info("Ingresa los datos actualizados.");
        String alias = Consola.leerTexto("Alias [" + d.getAlias() + "]");
        TipoDispositivo tipo = Consola.leerEnum("Tipo", TipoDispositivo.values());
        String so = leerSistemaOperativo(tipo, "Sistema operativo y versión [" + d.getSistemaOperativo() + "]");
        LocalDate fecha = leerFechaActualizacion(tipo);
        boolean antimalware = Consola.leerSiNo(tipo.getPreguntaProteccion());
        boolean cifrado = Consola.leerSiNo("¿Tiene el almacenamiento cifrado?");
        boolean bloqueo = Consola.leerSiNo("¿Tiene bloqueo de pantalla?");
        FrecuenciaWifi wifi = leerUsoWifi();
        boolean compartido = Consola.leerSiNo("¿Lo comparte con otras personas?");
        gestor.actualizar(est, d.getAlias(), alias.isBlank() ? d.getAlias() : alias, tipo,
                so.isBlank() ? d.getSistemaOperativo() : so, fecha, antimalware, cifrado, bloqueo, wifi, compartido);
        Consola.exito(String.format("Dispositivo actualizado. Índice de exposición: %.0f/100 (%s).", d.getPuntaje(), d.getClasificacion()));
    }

    private FrecuenciaWifi leerUsoWifi() {
        Consola.linea("¿Con qué frecuencia usas este dispositivo en redes WiFi públicas (cafeterías, centros comerciales, aeropuertos)?");
        Consola.info("Escala: 1 = Nunca, 2 = Casi nunca, 3 = A veces, 4 = Casi siempre, 5 = Siempre.");
        return FrecuenciaWifi.desdeEscala(Consola.leerEntero("Respuesta", FrecuenciaWifi.ESCALA_MIN, FrecuenciaWifi.ESCALA_MAX));
    }

    private String leerSistemaOperativo(TipoDispositivo tipo, String etiqueta) {
        Consola.info("Si no lo sabes, escribe \"no sé\". Dónde verlo: " + tipo.getGuiaInformacion());
        return Consola.leerTexto(etiqueta);
    }

    private LocalDate leerFechaActualizacion(TipoDispositivo tipo) {
        Consola.linea("¿Sabes la fecha de la última actualización?");
        Consola.linea("  1. Sí");
        Consola.linea("  2. No sé");
        if (Consola.leerEntero("Opción", 1, 2) == 2) {
            Consola.info("Se tomará como desconocida. En las recomendaciones verás cómo encontrarla.");
            return null;
        }
        return Consola.leerFecha("Fecha de la última actualización");
    }

    private void listar() {
        List<Dispositivo> lista = gestor.listar(est);
        if (lista.isEmpty()) Consola.info("No tienes dispositivos registrados.");
        for (Dispositivo d : lista) Consola.linea(d.toString() + (d.tieneAlerta() ? "  [ALERTA]" : ""));
    }

    private void mostrarDetalle(Dispositivo d) {
        String dias = d.conoceFechaActualizacion() ? d.getDiasSinActualizar() + " días sin actualizar" : "fecha de actualización: No sé";
        Consola.linea(String.format("%s | %s | SO: %s | %s", d.getAlias(), d.getTipoDispositivo(),
                d.getSistemaOperativo(), dias));
        if (Dispositivo.esSistemaSinSoporte(d.getSistemaOperativo())) {
            Consola.error("Tu sistema operativo ya no recibe actualizaciones de seguridad. Exposición: Crítica.");
        }
        Consola.linea("Recomendaciones:");
        for (String r : d.getRecomendaciones()) Consola.linea("  - " + r);
    }
}
