package negocio;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XposedOrNotCliente implements ServicioFiltracionesCorreo {
    private static final String URL = "https://api.xposedornot.com/v1/check-email/";
    private static final Pattern BLOQUE = Pattern.compile("\"breaches\"\\s*:\\s*\\[(.*?)\\]\\s*[,}]", Pattern.DOTALL);
    private static final Pattern TEXTO = Pattern.compile("\"([^\"]+)\"");

    @Override
    public List<String> buscarFiltraciones(String correo) throws ServicioNoDisponibleException {
        HttpResponse<String> r = ConexionHttp.get(URL + URLEncoder.encode(correo, StandardCharsets.UTF_8));
        String cuerpo = r.body();
        if (r.statusCode() == 404 || cuerpo.contains("Not found")) {
            return new ArrayList<>();
        }
        if (r.statusCode() == 429) {
            throw new ServicioNoDisponibleException("Se alcanzó el límite diario de consultas. Intenta mañana.");
        }
        if (r.statusCode() != 200) {
            throw new ServicioNoDisponibleException("XposedOrNot respondió con el código " + r.statusCode() + ".");
        }
        List<String> filtraciones = new ArrayList<>();
        Matcher bloque = BLOQUE.matcher(cuerpo);
        if (bloque.find()) {
            Matcher m = TEXTO.matcher(bloque.group(1));
            while (m.find()) {
                filtraciones.add(m.group(1));
            }
        }
        return filtraciones;
    }

    @Override
    public String getNombre() {
        return "XposedOrNot";
    }
}
