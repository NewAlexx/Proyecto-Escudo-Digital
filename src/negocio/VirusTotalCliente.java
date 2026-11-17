package negocio;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirusTotalCliente implements ServicioReputacion {
    private static final String BASE = "https://www.virustotal.com/api/v3";
    private static final Pattern MALICIOSOS = Pattern.compile("\"malicious\"\\s*:\\s*(\\d+)");
    private static final Pattern ID_ANALISIS = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final int ESPERA_MS = 20_000;

    private final String apiKey;

    public VirusTotalCliente(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public int analizarUrl(String url) throws ServicioNoDisponibleException {
        String id = Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
        HttpResponse<String> r = ConexionHttp.get(BASE + "/urls/" + id, "x-apikey", apiKey);
        if (r.statusCode() == 200) {
            return extraerMaliciosos(r.body());
        }
        verificarErrores(r);
        if (r.statusCode() == 404) {
            return enviarParaAnalisis(url);
        }
        throw new ServicioNoDisponibleException("VirusTotal respondió con el código " + r.statusCode() + ".");
    }

    private int enviarParaAnalisis(String url) throws ServicioNoDisponibleException {
        String cuerpo = "url=" + URLEncoder.encode(url, StandardCharsets.UTF_8);
        HttpResponse<String> r = ConexionHttp.postFormulario(BASE + "/urls", cuerpo, "x-apikey", apiKey);
        verificarErrores(r);
        Matcher m = ID_ANALISIS.matcher(r.body());
        if (r.statusCode() != 200 || !m.find()) {
            throw new ServicioNoDisponibleException("VirusTotal no aceptó el enlace para análisis.");
        }
        String idAnalisis = m.group(1);
        for (int intento = 0; intento < 3; intento++) {
            esperar();
            HttpResponse<String> a = ConexionHttp.get(BASE + "/analyses/" + idAnalisis, "x-apikey", apiKey);
            verificarErrores(a);
            if (a.statusCode() == 200 && a.body().contains("\"completed\"")) {
                return extraerMaliciosos(a.body());
            }
        }
        throw new ServicioNoDisponibleException("VirusTotal aún está analizando el enlace. Intenta más tarde.");
    }

    private void verificarErrores(HttpResponse<String> r) throws ServicioNoDisponibleException {
        if (r.statusCode() == 401) {
            throw new ServicioNoDisponibleException("La clave de VirusTotal no es válida. Revisa config.properties.");
        }
        if (r.statusCode() == 429) {
            throw new ServicioNoDisponibleException("Se alcanzó el límite de VirusTotal (4 por minuto o 500 por día).");
        }
    }

    private static int extraerMaliciosos(String json) {
        Matcher m = MALICIOSOS.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static void esperar() throws ServicioNoDisponibleException {
        try {
            Thread.sleep(ESPERA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioNoDisponibleException("La espera del análisis fue interrumpida.");
        }
    }
}
