package interfaz;

import modelo.Escenario;
import modelo.Estudiante;
import modelo.SesionEntrenamiento;
import negocio.GestorEntrenamiento;
import negocio.ValidacionException;

public class MenuEntrenamiento {
    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorEntrenamiento gestor;

    public MenuEntrenamiento(ContextoAplicacion ctx, Estudiante est) {
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorEntrenamiento();
    }

    public void mostrar() {
        while (true) {
            Consola.titulo("ENTRENAMIENTO ANTIPHISHING");
            Consola.linea("1. ¿Cómo reconocer un phishing? (tutorial)");
            Consola.linea("2. Empezar cuestionario (10 preguntas)");
            Consola.linea("0. Volver");
            Consola.info("Tu Índice de Resiliencia y el historial de cuestionarios están en \"Índice global de riesgo digital\".");
            int op = Consola.leerOpcion(2);
            if (op == 0) return;
            try {
                switch (op) {
                    case 1: tutorial(); break;
                    case 2: entrenar(); break;
                    default: break;
                }
            } catch (ValidacionException e) {
                Consola.error(e.getMessage());
            }
        }
    }

    private void tutorial() {
        Consola.titulo("¿CÓMO RECONOCER UN PHISHING?");
        for (String[] paso : GestorEntrenamiento.TUTORIAL) {
            Consola.linea(paso[0]);
            Consola.linea("   " + paso[1]);
        }
        Consola.info("Practica estas señales con la opción 2: Empezar cuestionario.");
    }

    private void entrenar() throws ValidacionException {
        SesionEntrenamiento s = gestor.iniciarSesion(est);
        Consola.info("Nivel inicial: " + s.getNivelActual() + ". Responde P (phishing), L (legítimo) o X para abandonar.");
        while (!s.estaCompleta()) {
            Escenario esc = gestor.siguienteEscenario(s);
            int nivelAntes = s.getNivelActual();
            Consola.linea("\n--- Pregunta " + (s.getCantidadPresentados() + 1) + "/10 (nivel " + esc.getDificultad() + ")");
            Consola.linea(esc.getTexto());
            String r;
            do {
                r = Consola.leerTexto("¿Phishing o legítimo? (P/L/X)").toUpperCase();
                if (!r.equals("P") && !r.equals("L") && !r.equals("X")) {
                    Consola.error("Selecciona una respuesta (P o L) antes de continuar.");
                }
            } while (!r.equals("P") && !r.equals("L") && !r.equals("X"));
            if (r.equals("X")) {
                gestor.finalizar(est, s);
            }
            boolean correcta = gestor.responder(s, esc, r.equals("P"));
            if (correcta) {
                Consola.exito("¡Correcto! Este mensaje era " + (esc.esPhishing() ? "phishing" : "legítimo") + ". " + esc.getExplicacion());
            } else if (esc.esPhishing()) {
                Consola.error("Incorrecto: marcaste como legítimo un mensaje fraudulento. En una situación real habrías caído en el engaño. "
                        + esc.getExplicacion());
            } else {
                Consola.error("Incorrecto: este mensaje era legítimo. " + esc.getExplicacion());
            }
            if (s.getNivelActual() > nivelAntes && !s.estaCompleta()) {
                Consola.info("¡Subiste al nivel " + s.getNivelActual() + "! Los siguientes escenarios serán más difíciles.");
            }
        }
        gestor.finalizar(est, s);
        double ir = gestor.calcularResiliencia(est);
        Consola.exito(String.format("Cuestionario completado: %d aciertos de 10. Puntaje: %.2f/100. Índice de resiliencia: %.2f (%s).",
                s.getAciertos(), s.getPuntaje(), ir, GestorEntrenamiento.nivelResiliencia(ir)));
        for (String rec : s.getRecomendaciones()) Consola.linea("  - " + rec);
    }
}
