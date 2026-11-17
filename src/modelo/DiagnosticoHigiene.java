package modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DiagnosticoHigiene extends Evaluacion {
    private static final long serialVersionUID = 1L;

    public static final String[] DIMENSIONES = {
        "Autenticación", "Actualización", "Correo y navegación", "Respaldo y protección", "Redes y aplicaciones"
    };
    public static final double[] PESOS = {0.30, 0.20, 0.20, 0.15, 0.15};

    private static final String[] RECOMENDACION_PRIORITARIA = {
        "Usa una contraseña distinta para cada página y activa la verificación en dos pasos en tu correo y tu banca.",
        "Activa las actualizaciones automáticas del sistema operativo, las aplicaciones y el antivirus.",
        "Antes de hacer clic, revisa el remitente y el destino real del enlace; nunca ingreses contraseñas desde un enlace recibido.",
        "Respalda tus trabajos en dos lugares (por ejemplo, la nube institucional y un disco externo) y activa el bloqueo y cifrado de tus equipos.",
        "No entres a cuentas importantes desde WiFi públicas sin VPN y revisa los permisos de cada aplicación que instalas."
    };

    private final int[] respuestas;
    private final double[] puntajesDimension;
    private final List<String> sitiosReutilizados;

    public DiagnosticoHigiene(int[] respuestas, List<String> sitiosReutilizados) {
        super();
        this.respuestas = Arrays.copyOf(respuestas, respuestas.length);
        this.puntajesDimension = new double[DIMENSIONES.length];
        this.sitiosReutilizados = new ArrayList<>(sitiosReutilizados);
        evaluar();
    }

    @Override
    public String getTipo() {
        return "Diagnóstico de higiene";
    }

    @Override
    public double calcularPuntaje() {
        double ihd = 0;
        for (int d = 0; d < DIMENSIONES.length; d++) {
            puntajesDimension[d] = (respuestas[2 * d] + respuestas[2 * d + 1] - 2) / 8.0 * 100.0;
            ihd += PESOS[d] * puntajesDimension[d];
        }
        return ihd;
    }

    @Override
    public String clasificar() {
        if (puntaje >= 80) return "Robusto";
        if (puntaje >= 60) return "Aceptable";
        if (puntaje >= 40) return "Vulnerable";
        return "Crítico";
    }

    public Integer[] getDimensionesOrdenadas() {
        Integer[] indices = {0, 1, 2, 3, 4};
        Arrays.sort(indices, Comparator.comparingDouble(i -> puntajesDimension[i]));
        return indices;
    }

    @Override
    public List<String> getRecomendaciones() {
        List<String> lista = new ArrayList<>();
        for (int d : getDimensionesOrdenadas()) {
            double pd = puntajesDimension[d];
            if (pd < 50) {
                lista.add("PRIORITARIA - " + DIMENSIONES[d] + ": " + RECOMENDACION_PRIORITARIA[d]);
            } else if (pd < 75) {
                lista.add("MEJORA - " + DIMENSIONES[d] + ": " + RECOMENDACION_PRIORITARIA[d]);
            } else {
                lista.add("BUENA PRÁCTICA - " + DIMENSIONES[d] + ": mantén tus hábitos actuales.");
            }
        }
        if (!sitiosReutilizados.isEmpty()) {
            lista.add(0, "Repites contraseñas en: " + String.join(", ", sitiosReutilizados)
                    + ". Cambia primero las del correo y la banca, porque permiten recuperar las demás cuentas.");
        }
        return lista;
    }

    public List<String> getDimensionesPrioritarias() {
        List<String> lista = new ArrayList<>();
        for (int d : getDimensionesOrdenadas()) {
            if (puntajesDimension[d] < 50) {
                lista.add(DIMENSIONES[d]);
            }
        }
        return lista;
    }

    public double getIHD() { return puntaje; }
    public double[] getPuntajesDimension() { return Arrays.copyOf(puntajesDimension, puntajesDimension.length); }
    public int[] getRespuestas() { return Arrays.copyOf(respuestas, respuestas.length); }
    public List<String> getSitiosReutilizados() { return new ArrayList<>(sitiosReutilizados); }
}
