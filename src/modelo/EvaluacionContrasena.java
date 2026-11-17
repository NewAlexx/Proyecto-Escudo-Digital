package modelo;

import java.util.ArrayList;
import java.util.List;

public class EvaluacionContrasena extends Evaluacion {
    private static final long serialVersionUID = 1L;
    public static final int SIN_CONSULTA = -1;
    public static final int NO_AUTORIZADA = -2;

    private final int longitud;
    private final int totalSumas;
    private final int totalRestas;
    private final List<String> detalle;
    private final List<String> explicaciones;
    private int vecesFiltrada;

    public EvaluacionContrasena(int longitud, int totalSumas, int totalRestas, List<String> detalle, List<String> explicaciones) {
        super();
        this.longitud = longitud;
        this.totalSumas = totalSumas;
        this.totalRestas = totalRestas;
        this.detalle = new ArrayList<>(detalle);
        this.explicaciones = new ArrayList<>(explicaciones);
        this.vecesFiltrada = NO_AUTORIZADA;
        evaluar();
    }

    @Override
    public String getTipo() {
        return "Contraseña evaluada";
    }

    @Override
    public double calcularPuntaje() {
        return Math.max(0, Math.min(100, totalSumas - totalRestas));
    }

    @Override
    public String clasificar() {
        if (estaComprometida()) {
            return "Comprometida";
        }
        return getNivelFortaleza();
    }

    public String getNivelFortaleza() {
        if (puntaje >= 80) return "Muy fuerte";
        if (puntaje >= 60) return "Fuerte";
        if (puntaje >= 40) return "Aceptable";
        if (puntaje >= 20) return "Débil";
        return "Muy débil";
    }

    public static final String[] GUIA_CONTRASENA_SEGURA = {
        "Usa al menos 12 caracteres; una frase de 3 o 4 palabras sin relación es fácil de recordar (ej.: Tren-Cactus-Lluvia-82).",
        "Combina mayúsculas, minúsculas, números y símbolos, sin secuencias como 123 o abc.",
        "No uses datos personales: nombres, fechas de nacimiento, mascotas ni la palabra de tu carrera.",
        "Usa una contraseña distinta para cada cuenta; si una se filtra, las demás siguen seguras.",
        "Genera contraseñas aleatorias con el generador gratuito de LastPass: https://www.lastpass.com/features/password-generator",
        "Guárdalas en un gestor de contraseñas gratuito y de código abierto como KeePassXC: https://keepassxc.org",
        "Activa la verificación en dos pasos en tu correo, tu banca y tus redes sociales."
    };

    public boolean necesitaGuia() {
        return estaComprometida() || puntaje < 60;
    }

    public void registrarFiltracion(int veces) {
        this.vecesFiltrada = veces;
        evaluar();
    }

    @Override
    public List<String> getRecomendaciones() {
        List<String> lista = new ArrayList<>();
        if (estaComprometida()) {
            lista.add("Esta contraseña apareció " + vecesFiltrada + " veces en filtraciones. No la uses en ninguna cuenta.");
        }
        if (longitud < 12) {
            lista.add("Usa al menos 12 caracteres; una frase de varias palabras es fácil de recordar y difícil de adivinar.");
        }
        for (String linea : detalle) {
            if (linea.startsWith("-")) {
                lista.add("Evita: " + linea.substring(1).replaceAll(":.*$", "").replaceAll("\\s*\\(.*\\)", "").trim() + ".");
            }
        }
        if (lista.isEmpty()) {
            lista.add("Buena contraseña. Úsala en un solo sitio y activa la verificación en dos pasos.");
        }
        return lista;
    }

    public boolean estaComprometida() { return vecesFiltrada > 0; }
    public boolean fueConsultada() { return vecesFiltrada >= 0; }
    public boolean fueAutorizada() { return vecesFiltrada != NO_AUTORIZADA; }
    public int getLongitud() { return longitud; }
    public int getTotalSumas() { return totalSumas; }
    public int getTotalRestas() { return totalRestas; }
    public int getVecesFiltrada() { return vecesFiltrada; }
    public List<String> getDetalle() { return new ArrayList<>(detalle); }
    public List<String> getExplicaciones() { return new ArrayList<>(explicaciones); }
}
