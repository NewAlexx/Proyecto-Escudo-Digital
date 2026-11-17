package interfaz;

import java.util.List;
import modelo.AnalisisMensaje;
import modelo.Estudiante;
import modelo.Indicador;
import negocio.GestorPhishing;
import negocio.ValidacionException;

public class MenuPhishing {
    private static final String[] CANALES = {"Correo", "Mensajería/SMS", "Red social"};

    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorPhishing gestor;

    public MenuPhishing(ContextoAplicacion ctx, Estudiante est) {
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorPhishing();
    }

    public void mostrar() {
        while (true) {
            Consola.titulo("ANALIZAR UN MENSAJE SOSPECHOSO");
            Consola.linea("1. ¿Cómo copiar un mensaje sin riesgo?");
            Consola.linea("2. Analizar un mensaje");
            Consola.linea("3. Analizar solo un enlace (VirusTotal)");
            Consola.linea("4. Historial de análisis");
            Consola.linea("5. Ver detalle de un análisis");
            Consola.linea("6. Eliminar un análisis");
            Consola.linea("0. Volver");
            int op = Consola.leerOpcion(6);
            if (op == 0) return;
            try {
                switch (op) {
                    case 1: instrucciones(); break;
                    case 2: analizar(); break;
                    case 3: analizarEnlace(); break;
                    case 4: gestor.listar(est).forEach(a -> Consola.linea(a + "  " + (a.esSoloEnlace() ? a.getEnlaces().get(0) : a.getRemitente()))); break;
                    case 5: mostrarDetalle(gestor.buscar(est, Consola.leerTexto("Código"))); break;
                    case 6: gestor.eliminar(est, Consola.leerTexto("Código")); Consola.exito("Análisis eliminado."); break;
                    default: break;
                }
            } catch (ValidacionException e) {
                Consola.error(e.getMessage());
            }
        }
    }

    private void instrucciones() {
        Consola.linea("- Copia el texto del mensaje y pégalo aquí. No respondas al mensaje.");
        Consola.linea("- Enlaces: clic derecho > 'Copiar dirección del enlace'. Nunca hagas clic en el enlace.");
        Consola.linea("- Si el enlace es una imagen o un botón, no lo toques: pasa el mouse encima y anota la dirección que aparece.");
        Consola.linea("- Adjuntos: NO los descargues ni los abras. Escudo Digital no analiza archivos adjuntos.");
        Consola.linea("- VirusTotal visita los enlaces desde sus propios servidores; tu equipo no abre la página.");
    }

    private void analizar() throws ValidacionException {
        instrucciones();
        Consola.linea("Canal por el que lo recibiste:");
        for (int i = 0; i < CANALES.length; i++) Consola.linea("  " + (i + 1) + ". " + CANALES[i]);
        String canal = CANALES[Consola.leerEntero("Elige", 1, CANALES.length) - 1];
        String remitente = Consola.leerTexto("Remitente (opcional: correo, número o nombre que aparece; Enter si no lo sabes)");
        String asunto = Consola.leerTexto("Asunto (opcional)");
        String cuerpo = Consola.leerMultilinea("Cuerpo del mensaje");
        List<String> enlaces = Consola.leerLista("Enlaces adicionales copiados sin abrirlos");
        if (!gestor.virusTotalDisponible()) {
            Consola.info("VirusTotal no está configurado: se hará solo el análisis de texto.");
        } else {
            Consola.info("Consultando VirusTotal... puede tardar hasta un minuto si el enlace es nuevo.");
        }
        AnalisisMensaje a = gestor.analizarMensaje(est, canal, remitente, asunto, cuerpo, enlaces);
        Consola.exito(String.format("Análisis completado: %.0f/100 — %s. Se detectaron %d indicadores.",
                a.getPuntaje(), a.getClasificacion(), a.getIndicadores().size()));
        mostrarDetalle(a);
    }

    private void analizarEnlace() throws ValidacionException {
        Consola.info("Copia el enlace sin abrirlo (clic derecho > 'Copiar dirección del enlace') y pégalo aquí.");
        String enlace = Consola.leerTexto("Enlace a analizar");
        Consola.info(gestor.virusTotalDisponible()
                ? "Consultando VirusTotal... puede tardar hasta un minuto si el enlace es nuevo."
                : "VirusTotal no está configurado: solo se revisará el formato del enlace.");
        AnalisisMensaje a = gestor.analizarEnlace(est, enlace);
        Consola.exito(String.format("Análisis completado: %.0f/100 — %s.", a.getPuntaje(), a.getClasificacion()));
        mostrarDetalle(a);
    }

    private void mostrarDetalle(AnalisisMensaje a) {
        if (a.esSoloEnlace()) {
            Consola.linea("Enlace analizado: " + a.getEnlaces().get(0));
        } else {
            Consola.linea("Remitente: " + a.getRemitente() + " | Canal: " + a.getCanal());
        }
        if (!a.esSoloEnlace() && !a.getRemitente().contains("@")) {
            Consola.info("Sin el correo del remitente no se pudo revisar si su dominio imita a uno legítimo (I1 e I2).");
        }
        if (a.getIndicadores().isEmpty()) Consola.linea("No se encontraron indicadores de phishing.");
        for (Indicador i : a.getIndicadores()) Consola.linea("  " + i);
        for (String r : a.getResultadosVirusTotal()) Consola.linea("  [VirusTotal] " + r);
        Consola.linea("Recomendaciones:");
        for (String r : a.getRecomendaciones()) Consola.linea("  - " + r);
    }
}
