package negocio;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeakCheckCliente implements ServicioFiltracionesCorreo {
    private static final String URL = "https://leakcheck.io/api/public?check=";
    private static final Pattern NOMBRE = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

    @Override
    public List<String> buscarFiltraciones(String correo) throws ServicioNoDisponibleException {
        HttpResponse<String> r = ConexionHttp.get(URL + URLEncoder.encode(correo, StandardCharsets.UTF_8));
        String cuerpo = r.body();
        if (r.statusCode() == 429) {
            throw new ServicioNoDisponibleException("Se alcanzó el límite de consultas de LeakCheck.");
        }
        if (r.statusCode() != 200) {
            throw new ServicioNoDisponibleException("LeakCheck respondió con el código " + r.statusCode() + ".");
        }
        List<String> filtraciones = new ArrayList<>();
        if (cuerpo.contains("\"success\":false") || cuerpo.contains("\"success\": false")) {
            if (cuerpo.toLowerCase().contains("not found")) {
                return filtraciones;
            }
            throw new ServicioNoDisponibleException("LeakCheck no pudo procesar la consulta.");
        }
        Matcher m = NOMBRE.matcher(cuerpo);
        while (m.find()) {
            if (!filtraciones.contains(m.group(1))) {
                filtraciones.add(m.group(1));
            }
        }
        return filtraciones;
    }

    @Override
    public String getNombre() {
        return "LeakCheck";
    }
}
