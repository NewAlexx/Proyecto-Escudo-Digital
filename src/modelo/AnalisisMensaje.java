package modelo;

import java.util.ArrayList;
import java.util.List;

public class AnalisisMensaje extends Evaluacion {
    public static final String CANAL_SOLO_ENLACE = "Solo enlace";
    private static final long serialVersionUID = 1L;

    private final String canal;
    private final String remitente;
    private final String asunto;
    private final List<String> enlaces;
    private final List<Indicador> indicadores;
    private final List<String> resultadosVirusTotal;

    public AnalisisMensaje(String canal, String remitente, String asunto, List<String> enlaces,
                           List<Indicador> indicadores, List<String> resultadosVirusTotal) {
        super();
        this.canal = canal;
        this.remitente = remitente;
        this.asunto = asunto;
        this.enlaces = new ArrayList<>(enlaces);
        this.indicadores = new ArrayList<>(indicadores);
        this.resultadosVirusTotal = new ArrayList<>(resultadosVirusTotal);
        evaluar();
    }

    @Override
    public String getTipo() {
        return "Análisis de mensaje";
    }

    @Override
    public double calcularPuntaje() {
        int suma = 0;
        for (Indicador i : indicadores) {
            suma += i.getPeso();
        }
        return Math.min(100, suma);
    }

    @Override
    public String clasificar() {
        if (puntaje >= 60) return "Phishing probable";
        if (puntaje >= 30) return "Sospechoso";
        return "Probablemente legítimo";
    }

    @Override
    public List<String> getRecomendaciones() {
        List<String> lista = new ArrayList<>();
        if (esSoloEnlace()) {
            if (puntaje >= 60) {
                lista.add("No abras este enlace ni lo compartas. Si te llegó en un mensaje, repórtalo al área de soporte tecnológico.");
            } else if (puntaje >= 30) {
                lista.add("Desconfía de este enlace: entra al sitio oficial escribiendo tú mismo la dirección en el navegador.");
            } else {
                lista.add(indicadores.isEmpty()
                        ? "No se encontraron señales de riesgo, pero nunca ingreses contraseñas desde un enlace que te enviaron."
                        : "El riesgo es bajo, pero el enlace tiene señales que conviene revisar (arriba). "
                          + "Nunca ingreses contraseñas desde un enlace que te enviaron.");
            }
        } else {
            if (puntaje >= 60) {
                lista.add("No hagas clic en los enlaces ni respondas. Reporta el mensaje al área de soporte tecnológico.");
            } else if (puntaje >= 30) {
                lista.add("Verifica el mensaje por un canal oficial antes de realizar cualquier acción.");
            } else {
                lista.add("Parece legítimo, pero desconfía si te pide datos personales, contraseñas o pagos.");
            }
            lista.add("Si el mensaje trae archivos adjuntos, no los descargues ni los abras: Escudo Digital no los analiza.");
        }
        if (!resultadosVirusTotal.isEmpty() && !resultadosVirusTotal.get(0).startsWith("VirusTotal no está configurado")) {
            lista.add("VirusTotal puede tener falsos positivos y falsos negativos: úsalo como una señal más, no como garantía.");
        }
        return lista;
    }

    public boolean esSoloEnlace() { return CANAL_SOLO_ENLACE.equals(canal); }
    public String getCanal() { return canal; }
    public String getRemitente() { return remitente; }
    public String getAsunto() { return asunto; }
    public List<String> getEnlaces() { return new ArrayList<>(enlaces); }
    public List<Indicador> getIndicadores() { return new ArrayList<>(indicadores); }
    public List<String> getResultadosVirusTotal() { return new ArrayList<>(resultadosVirusTotal); }
}
