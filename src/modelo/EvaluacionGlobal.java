package modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EvaluacionGlobal extends Evaluacion {
    private static final long serialVersionUID = 1L;

    public static final String[] COMPONENTES = {
        "Higiene digital", "Credenciales", "Correo expuesto", "Dispositivos", "Resiliencia ante phishing"
    };
    public static final double[] PESOS = {0.25, 0.25, 0.15, 0.15, 0.20};
    private static final String[] ACCIONES = {
        "Repite el diagnóstico de higiene digital y aplica sus recomendaciones prioritarias.",
        "Cambia las contraseñas débiles o comprometidas por otras generadas con LastPass y guárdalas en KeePassXC.",
        "Cambia las contraseñas de los servicios filtrados (genéralas con LastPass y guárdalas en KeePassXC) y activa la verificación en dos pasos.",
        "Actualiza tus dispositivos y activa cifrado, bloqueo de pantalla y antimalware.",
        "Completa más cuestionarios de entrenamiento antiphishing y revisa el tutorial para reconocer phishing."
    };

    private final double[] riesgos;
    private final List<String> datosFaltantes;
    private final String motivoEscalamiento;
    private String tendencia;

    public EvaluacionGlobal(double[] riesgos, List<String> datosFaltantes, String motivoEscalamiento) {
        super();
        this.riesgos = Arrays.copyOf(riesgos, riesgos.length);
        this.datosFaltantes = new ArrayList<>(datosFaltantes);
        this.motivoEscalamiento = motivoEscalamiento;
        this.tendencia = "Sin evaluación anterior";
        evaluar();
    }

    @Override
    public double calcularPuntaje() {
        double igrd = 0;
        for (int i = 0; i < riesgos.length; i++) {
            igrd += PESOS[i] * riesgos[i];
        }
        if (motivoEscalamiento != null) {
            igrd = Math.max(igrd, 75);
        }
        return igrd;
    }

    @Override
    public String clasificar() {
        if (puntaje >= 75) return "Crítico";
        if (puntaje >= 50) return "Alto";
        if (puntaje >= 25) return "Moderado";
        return "Bajo";
    }

    @Override
    public String getTipo() {
        return "Riesgo global";
    }

    @Override
    public List<String> getRecomendaciones() {
        Integer[] orden = {0, 1, 2, 3, 4};
        Arrays.sort(orden, Comparator.comparingDouble((Integer i) -> riesgos[i]).reversed());
        List<String> plan = new ArrayList<>();
        for (int k = 0; k < 3; k++) {
            int i = orden[k];
            plan.add(String.format("%d) %s (riesgo %.2f): %s", k + 1, COMPONENTES[i], riesgos[i], ACCIONES[i]));
        }
        return plan;
    }

    public void setTendencia(String tendencia) { this.tendencia = tendencia; }
    public String getTendencia() { return tendencia; }
    public double[] getRiesgos() { return Arrays.copyOf(riesgos, riesgos.length); }
    public List<String> getDatosFaltantes() { return new ArrayList<>(datosFaltantes); }
    public String getMotivoEscalamiento() { return motivoEscalamiento; }
}
