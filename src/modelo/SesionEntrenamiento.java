package modelo;

import java.util.ArrayList;
import java.util.List;

public class SesionEntrenamiento extends Evaluacion {
    private static final long serialVersionUID = 1L;
    public static final int ESCENARIOS_POR_SESION = 10;

    private final List<Escenario> escenarios;
    private int nivelActual;
    private int aciertosSeguidos;
    private int aciertos;
    private int falsosPositivos;
    private int falsosNegativos;
    private int puntos;
    private int puntosMaximos;

    public SesionEntrenamiento(int nivelInicial) {
        super();
        this.escenarios = new ArrayList<>();
        this.nivelActual = nivelInicial;
        this.puntaje = 0;
        this.clasificacion = "En curso";
    }

    public boolean registrarRespuesta(Escenario escenario, boolean marcoPhishing) {
        escenarios.add(escenario);
        int d = escenario.getDificultad();
        puntosMaximos += 10 * d;
        boolean correcta = marcoPhishing == escenario.esPhishing();
        if (correcta) {
            aciertos++;
            puntos += 10 * d;
            aciertosSeguidos++;
            if (aciertosSeguidos == 2) {
                nivelActual = Math.min(3, nivelActual + 1);
                aciertosSeguidos = 0;
            }
        } else {
            if (marcoPhishing) {
                falsosPositivos++;
            } else {
                falsosNegativos++;
                puntos -= 5 * d;
            }
            nivelActual = Math.max(1, nivelActual - 1);
            aciertosSeguidos = 0;
        }
        return correcta;
    }

    public boolean yaPresentado(Escenario escenario) {
        return escenarios.contains(escenario);
    }

    public boolean estaCompleta() {
        return escenarios.size() == ESCENARIOS_POR_SESION;
    }

    @Override
    public String getTipo() {
        return "Sesión de entrenamiento";
    }

    @Override
    public double calcularPuntaje() {
        if (puntosMaximos == 0) return 0;
        return Math.max(0, puntos) * 100.0 / puntosMaximos;
    }

    @Override
    public String clasificar() {
        if (puntaje >= 85) return "Experto";
        if (puntaje >= 70) return "Competente";
        if (puntaje >= 50) return "En formación";
        return "Vulnerable";
    }

    @Override
    public List<String> getRecomendaciones() {
        List<String> lista = new ArrayList<>();
        if (falsosNegativos > 0) {
            lista.add("Marcaste " + falsosNegativos + " mensaje(s) fraudulento(s) como legítimos. Revisa remitentes, enlaces y urgencia.");
        }
        if (falsosPositivos > 0) {
            lista.add("Desconfiaste de " + falsosPositivos + " mensaje(s) legítimo(s). Es mejor verificar que caer, pero confirma por canales oficiales.");
        }
        if (lista.isEmpty()) lista.add("¡Excelente! Reconociste todos los escenarios.");
        return lista;
    }

    public int getNivelActual() { return nivelActual; }
    public int getCantidadPresentados() { return escenarios.size(); }
    public int getAciertos() { return aciertos; }
    public int getFalsosPositivos() { return falsosPositivos; }
    public int getFalsosNegativos() { return falsosNegativos; }
    public int getPuntos() { return puntos; }
    public int getPuntosMaximos() { return puntosMaximos; }
}
