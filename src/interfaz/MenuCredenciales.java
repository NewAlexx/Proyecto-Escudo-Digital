package interfaz;

import modelo.ConsultaCorreo;
import modelo.Estudiante;
import modelo.EvaluacionContrasena;
import negocio.GestorCredenciales;
import negocio.ServicioNoDisponibleException;
import negocio.ValidacionException;

public class MenuCredenciales {
    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorCredenciales gestor;

    public MenuCredenciales(ContextoAplicacion ctx, Estudiante est) {
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorCredenciales();
    }

    public void mostrar() {
        while (true) {
            Consola.titulo("CONTRASEÑAS Y CORREOS FILTRADOS");
            Consola.linea("1. Evaluar una contraseña");
            Consola.linea("2. Verificar si mi correo aparece en filtraciones");
            Consola.linea("0. Volver");
            int op = Consola.leerOpcion(2);
            if (op == 0) return;
            try {
                switch (op) {
                    case 1: evaluarContrasena(); break;
                    case 2: consultarCorreo(); break;
                    default: break;
                }
            } catch (ValidacionException e) {
                Consola.error(e.getMessage());
            }
        }
    }

    private void evaluarContrasena() throws ValidacionException {
        Consola.info("La contraseña no se guarda. Primero se evalúa en este equipo, sin enviarla a Internet.");
        char[] clave = Consola.leerContrasena("Contraseña a evaluar");
        EvaluacionContrasena ev;
        try {
            ev = gestor.evaluarFortaleza(est, clave);
            Consola.titulo("RESULTADO: ¿ES SEGURA TU CONTRASEÑA?");
            String msg = String.format("Fortaleza: %s (%.0f/100). Puntos a favor: %d | Puntos en contra: %d.",
                    ev.getNivelFortaleza(), ev.getPuntaje(), ev.getTotalSumas(), ev.getTotalRestas());
            if (ev.getPuntaje() >= 60) {
                Consola.exito(msg);
                Consola.exito("Por su estructura, tu contraseña es SEGURA.");
            } else {
                Consola.error(msg);
                Consola.error("Por su estructura, tu contraseña NO es segura.");
            }
            Consola.titulo("¿VERIFICAR SI FUE FILTRADA?");
            Consola.linea("Una contraseña fuerte puede no servir si ya apareció en una filtración de datos.");
            Consola.linea("Escudo Digital puede comprobarlo con Pwned Passwords (Have I Been Pwned):");
            Consola.linea("  - Tu contraseña NO se envía. Solo se envían los 5 primeros caracteres de su hash SHA-1.");
            Consola.linea("  - El servicio responde con una lista de hashes parecidos y la comparación se hace en tu equipo.");
            Consola.linea("1. Sí, verificar mi contraseña");
            Consola.linea("2. No, continuar sin verificar");
            boolean autoriza = Consola.leerEntero("Opción", 1, 2) == 1;
            gestor.verificarFiltracion(ev, clave, autoriza);
        } finally {
            java.util.Arrays.fill(clave, '\0');
        }
        if (!ev.fueAutorizada()) {
            Consola.info("No se consultó Pwned Passwords. Se muestra solo el puntaje de fortaleza.");
        } else if (!ev.fueConsultada()) {
            Consola.error("No se pudo conectar con Pwned Passwords. Se muestra solo el puntaje de fortaleza.");
        } else if (ev.estaComprometida()) {
            Consola.error("Alerta: esta contraseña apareció " + ev.getVecesFiltrada() + " veces en filtraciones. No la uses.");
        } else {
            Consola.exito("Esta contraseña no aparece en filtraciones conocidas.");
        }
        Consola.linea("Recomendaciones:");
        for (String r : ev.getRecomendaciones()) Consola.linea("  - " + r);
        if (ev.necesitaGuia()) {
            Consola.titulo("GUÍA: CÓMO CREAR UNA CONTRASEÑA SEGURA");
            for (String g : EvaluacionContrasena.GUIA_CONTRASENA_SEGURA) Consola.linea("  - " + g);
        }
        Consola.linea("");
        Consola.linea("1. ¿Por qué obtuve este puntaje? (explicación detallada)");
        Consola.linea("0. Continuar");
        if (Consola.leerOpcion(1) == 1) explicarPuntaje(ev);
    }

    private void explicarPuntaje(EvaluacionContrasena ev) {
        Consola.titulo("¿POR QUÉ OBTUVE " + Math.round(ev.getPuntaje()) + " PUNTOS?");
        Consola.linea("El puntaje empieza en 0. Suman las características que hacen difícil adivinar la contraseña");
        Consola.linea("y restan los patrones que la hacen predecible. Puntaje = sumas − restas, entre 0 y 100.");
        Consola.linea("Niveles: 0-19 Muy débil, 20-39 Débil, 40-59 Aceptable, 60-79 Fuerte, 80-100 Muy fuerte.");
        java.util.List<String> detalle = ev.getDetalle();
        java.util.List<String> explicaciones = ev.getExplicaciones();
        Consola.linea("\n--- LO QUE SUMA (" + ev.getTotalSumas() + " puntos)");
        for (int i = 0; i < detalle.size(); i++) {
            if (detalle.get(i).startsWith("+")) {
                Consola.linea("  " + detalle.get(i));
                Consola.linea("      " + explicaciones.get(i));
            }
        }
        Consola.linea("\n--- LO QUE RESTA (" + ev.getTotalRestas() + " puntos)");
        boolean hayRestas = false;
        for (int i = 0; i < detalle.size(); i++) {
            if (detalle.get(i).startsWith("-")) {
                hayRestas = true;
                Consola.linea("  " + detalle.get(i));
                Consola.linea("      " + explicaciones.get(i));
            }
        }
        if (!hayRestas) Consola.linea("  No se encontraron patrones predecibles.");
        Consola.linea(String.format("%nResultado: %d − %d = %.0f puntos (%s).", ev.getTotalSumas(), ev.getTotalRestas(),
                ev.getPuntaje(), ev.getNivelFortaleza()));
    }

    private void consultarCorreo() throws ValidacionException {
        String correo = Consola.leerTexto("Correo a consultar");
        Consola.info("Tu correo se enviará a XposedOrNot (o a LeakCheck si el primero no responde).");
        boolean consentimiento = Consola.leerSiNo("¿Das tu consentimiento para enviarlo?");
        try {
            ConsultaCorreo c = gestor.consultarCorreo(est, correo, consentimiento);
            if (c.getFiltraciones().isEmpty()) {
                Consola.exito("Tu correo no aparece en filtraciones conocidas. (Fuente: " + c.getFuente() + ")");
            } else {
                Consola.error("Tu correo aparece en " + c.getFiltraciones().size() + " filtraciones: "
                        + String.join(", ", c.getFiltraciones()) + ". Nivel de exposición: " + c.getClasificacion()
                        + ". (Fuente: " + c.getFuente() + ")");
            }
            for (String r : c.getRecomendaciones()) Consola.linea("  - " + r);
        } catch (ServicioNoDisponibleException e) {
            Consola.error(e.getMessage());
        }
    }
}
