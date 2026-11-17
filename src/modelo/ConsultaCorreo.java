package modelo;

import java.util.ArrayList;
import java.util.List;

public class ConsultaCorreo extends Evaluacion {
    private static final long serialVersionUID = 1L;

    private final String correoEnmascarado;
    private final List<String> filtraciones;
    private final String fuente;

    public ConsultaCorreo(String correo, List<String> filtraciones, String fuente) {
        super();
        this.correoEnmascarado = enmascarar(correo);
        this.filtraciones = new ArrayList<>(filtraciones);
        this.fuente = fuente;
        evaluar();
    }

    public static String enmascarar(String correo) {
        int arroba = correo.indexOf('@');
        if (arroba <= 1) {
            return "***" + correo.substring(Math.max(arroba, 0));
        }
        return correo.charAt(0) + "***" + correo.substring(arroba);
    }

    @Override
    public String getTipo() {
        return "Consulta de correo";
    }

    @Override
    public double calcularPuntaje() {
        int n = filtraciones.size();
        if (n == 0) return 0;
        if (n <= 2) return 40;
        if (n <= 5) return 70;
        return 100;
    }

    @Override
    public String clasificar() {
        int n = filtraciones.size();
        if (n == 0) return "Sin exposición";
        if (n <= 2) return "Moderada";
        if (n <= 5) return "Alta";
        return "Crítica";
    }

    @Override
    public List<String> getRecomendaciones() {
        List<String> lista = new ArrayList<>();
        if (filtraciones.isEmpty()) {
            lista.add("Tu correo no aparece en filtraciones conocidas. Mantén contraseñas únicas y la verificación en dos pasos.");
            return lista;
        }
        for (String f : filtraciones) {
            lista.add("Cambia la contraseña que usabas en " + f + " y en cualquier otro sitio donde la hayas repetido.");
        }
        lista.add("Genera las nuevas contraseñas con LastPass (lastpass.com/features/password-generator) y guárdalas en KeePassXC (keepassxc.org).");
        lista.add("Activa la verificación en dos pasos en tu correo: es la cuenta que permite recuperar todas las demás.");
        return lista;
    }

    public String getCorreoEnmascarado() { return correoEnmascarado; }
    public List<String> getFiltraciones() { return new ArrayList<>(filtraciones); }
    public String getFuente() { return fuente; }
}
