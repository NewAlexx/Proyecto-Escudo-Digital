package interfaz;

import modelo.Estudiante;
import modelo.EvaluacionGlobal;
import negocio.GestorEntrenamiento;
import negocio.GestorRiesgoGlobal;
import negocio.ValidacionException;

public class MenuRiesgo {
    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorRiesgoGlobal gestor;

    public MenuRiesgo(ContextoAplicacion ctx, Estudiante est) {
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorRiesgoGlobal();
    }

    public void mostrar() {
        while (true) {
            Consola.titulo("ÍNDICE GLOBAL DE RIESGO DIGITAL");
            Consola.linea("1. Calcular mi índice");
            Consola.linea("2. Historial de evaluaciones");
            Consola.linea("3. Índice de Resiliencia e historial de cuestionarios");
            Consola.linea("0. Volver");
            int op = Consola.leerOpcion(3);
            if (op == 0) return;
            try {
                if (op == 1) calcular();
                else if (op == 2) gestor.listar(est).forEach(g -> Consola.linea(g.toString()));
                else resiliencia();
            } catch (ValidacionException e) {
                Consola.error(e.getMessage());
            }
        }
    }

    private void resiliencia() {
        GestorEntrenamiento ge = ctx.getGestorEntrenamiento();
        if (ge.listar(est).isEmpty()) {
            Consola.info("Aún no completas ningún cuestionario. Hazlo en \"Entrenamiento antiphishing\".");
            return;
        }
        ge.listar(est).forEach(s -> Consola.linea(s.toString()));
        double ir = ge.calcularResiliencia(est);
        Consola.linea(String.format("Índice de Resiliencia: %.2f (%s). Cuestionarios hoy: %d de %d.", ir,
                GestorEntrenamiento.nivelResiliencia(ir), ge.sesionesDeHoy(est), GestorEntrenamiento.MAX_SESIONES_DIARIAS));
    }

    private void calcular() throws ValidacionException {
        EvaluacionGlobal g = gestor.calcularIndice(est);
        double[] r = g.getRiesgos();
        for (int i = 0; i < r.length; i++) {
            Consola.linea(String.format("  %-26s riesgo %6.2f  (peso %.2f)", EvaluacionGlobal.COMPONENTES[i], r[i], EvaluacionGlobal.PESOS[i]));
        }
        for (String falta : g.getDatosFaltantes()) {
            Consola.info("Datos incompletos: aún no registras " + falta + "; se usó un valor de 50.");
        }
        if (g.getMotivoEscalamiento() != null) {
            Consola.error("Riesgo elevado a Crítico: " + g.getMotivoEscalamiento() + ".");
        }
        Consola.exito(String.format("Tu Índice Global de Riesgo Digital es %.2f/100 (%s).", g.getPuntaje(), g.getClasificacion()));
        Consola.linea("Tendencia: " + g.getTendencia());
        if (g.getClasificacion().equals("Bajo")) Consola.exito("¡Excelente! Tu riesgo digital es Bajo. Mantén tus buenas prácticas.");
        Consola.linea("Plan de acción:");
        for (String p : g.getRecomendaciones()) Consola.linea("  " + p);
    }
}
