package negocio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ConexionHttp {
    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final String AGENTE = "EscudoDigital-UDLA/1.0";

    private ConexionHttp() { }

    public static HttpResponse<String> get(String url, String... cabeceras) throws ServicioNoDisponibleException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                .header("User-Agent", AGENTE).GET();
        agregarCabeceras(b, cabeceras);
        return enviar(b.build());
    }

    public static HttpResponse<String> postFormulario(String url, String cuerpo, String... cabeceras) throws ServicioNoDisponibleException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                .header("User-Agent", AGENTE)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo));
        agregarCabeceras(b, cabeceras);
        return enviar(b.build());
    }

    private static void agregarCabeceras(HttpRequest.Builder b, String... cabeceras) {
        for (int i = 0; i + 1 < cabeceras.length; i += 2) {
            b.header(cabeceras[i], cabeceras[i + 1]);
        }
    }

    private static HttpResponse<String> enviar(HttpRequest peticion) throws ServicioNoDisponibleException {
        try {
            return CLIENTE.send(peticion, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ServicioNoDisponibleException("No hay conexión con " + peticion.uri().getHost() + ".");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioNoDisponibleException("La consulta fue interrumpida.");
        }
    }
}
