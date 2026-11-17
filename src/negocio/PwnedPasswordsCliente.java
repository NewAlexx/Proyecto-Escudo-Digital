package negocio;

import java.net.http.HttpResponse;

public class PwnedPasswordsCliente implements ServicioContrasenasFiltradas {
    private static final String URL = "https://api.pwnedpasswords.com/range/";

    @Override
    public int contarApariciones(String sha1Hex) throws ServicioNoDisponibleException {
        String prefijo = sha1Hex.substring(0, 5);
        String sufijo = sha1Hex.substring(5);
        HttpResponse<String> r = ConexionHttp.get(URL + prefijo, "Add-Padding", "true");
        if (r.statusCode() != 200) {
            throw new ServicioNoDisponibleException("Pwned Passwords respondió con el código " + r.statusCode() + ".");
        }
        for (String linea : r.body().split("\\r?\\n")) {
            String[] partes = linea.trim().split(":");
            if (partes.length == 2 && partes[0].equalsIgnoreCase(sufijo)) {
                return Integer.parseInt(partes[1].trim());
            }
        }
        return 0;
    }
}
