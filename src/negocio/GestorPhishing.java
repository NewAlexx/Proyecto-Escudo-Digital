package negocio;

import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import modelo.AnalisisMensaje;
import modelo.Estudiante;
import modelo.Indicador;

public class GestorPhishing {
    public static final int MAX_ENLACES_VIRUSTOTAL = 4;

    private static final List<String> DATOS_SENSIBLES = Arrays.asList(
        "contrasena", "clave", "pin", "codigo de verificacion", "codigo de seguridad", "numero de tarjeta",
        "tarjeta de credito", "cvv", "cedula", "verifique su cuenta", "verifica tu cuenta", "confirme sus datos",
        "confirma tus datos", "actualice sus datos", "actualiza tus datos", "inicie sesion", "datos bancarios");
    private static final List<String> URGENCIA = Arrays.asList(
        "urgente", "inmediatamente", "24 horas", "48 horas", "suspendida", "suspendera", "bloqueada", "bloqueara",
        "ultimo aviso", "sera eliminada", "accion requerida", "de inmediato", "evitar recargos");
    private static final List<String> SALUDO_GENERICO = Arrays.asList(
        "estimado usuario", "estimado cliente", "querido estudiante", "estimado estudiante", "querido cliente");
    private static final List<String> PREMIOS = Arrays.asList(
        "felicidades", "ha sido seleccionado", "fuiste seleccionado", "premio", "ganaste", "ha ganado", "beca",
        "reembolso", "dinero gratis", "sin experiencia");
    private static final List<String> DOMINIOS_LEGITIMOS = Arrays.asList(
        "udla.edu.ec", "gmail.com", "outlook.com", "hotmail.com", "yahoo.com", "microsoft.com", "office.com",
        "google.com", "facebook.com", "instagram.com", "whatsapp.com", "apple.com", "paypal.com", "netflix.com",
        "github.com", "pichincha.com", "bancoguayaquil.com", "produbanco.com.ec", "bancodelpacifico.com");
    private static final List<String> PROVEEDORES_GRATUITOS = Arrays.asList(
        "gmail.com", "hotmail.com", "outlook.com", "yahoo.com", "live.com", "icloud.com", "protonmail.com");
    private static final List<String> MENCIONES_INSTITUCION = Arrays.asList(
        "udla", "universidad", "banco", "pichincha", "produbanco", "pacifico", "tesoreria", "rectorado");
    private static final List<String> ACORTADORES = Arrays.asList(
        "bit.ly", "tinyurl.com", "cutt.ly", "is.gd", "t.co", "goo.gl", "ow.ly", "rebrand.ly", "shorturl.at");
    private static final Pattern ENLACE = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);
    private static final Pattern IP = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");
    private static final Pattern REMITENTE_CORREO = Pattern.compile("[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
    public static final String REMITENTE_DESCONOCIDO = "Desconocido";
    public static final int MAX_REMITENTE = 100;
    public static final String CANAL_SOLO_ENLACE = AnalisisMensaje.CANAL_SOLO_ENLACE;
    private final ServicioReputacion servicioReputacion;

    public GestorPhishing(ServicioReputacion servicioReputacion) {
        this.servicioReputacion = servicioReputacion;
    }

    public boolean virusTotalDisponible() {
        return servicioReputacion != null;
    }

    public AnalisisMensaje analizarMensaje(Estudiante estudiante, String canal, String remitente, String asunto,
                                           String cuerpo, List<String> enlacesManuales)
            throws ValidacionException {
        validar(canal, remitente, cuerpo);
        if (remitente == null || remitente.isBlank()) remitente = REMITENTE_DESCONOCIDO;
        String texto = normalizar((asunto == null ? "" : asunto) + " " + cuerpo);
        String textoSinLeet = quitarLeetspeak(texto);
        List<String> enlaces = extraerEnlaces(cuerpo, enlacesManuales);
        List<Indicador> indicadores = new ArrayList<>();

        List<String> dominios = new ArrayList<>();
        String dominioRemitente = dominioDeRemitente(remitente);
        if (dominioRemitente != null) dominios.add(dominioRemitente);
        for (String e : enlaces) {
            String h = host(e);
            if (h != null) dominios.add(h);
        }
        for (String d : dominios) {
            String parecido = dominioImitado(d);
            if (parecido != null) {
                indicadores.add(new Indicador("I1", "El dominio '" + d + "' imita a '" + parecido + "' (diferencia de "
                        + distanciaLevenshtein(d, parecido) + " caracteres).", 20));
                break;
            }
        }
        if (dominioRemitente != null && PROVEEDORES_GRATUITOS.contains(dominioRemitente)
                && !encontrarFrases(texto, MENCIONES_INSTITUCION).isEmpty()) {
            indicadores.add(new Indicador("I2", "Menciona a la universidad o a un banco, pero el remitente usa un correo gratuito ("
                    + dominioRemitente + ").", 15));
        }
        List<String> sensibles = encontrarFrases(texto, textoSinLeet, DATOS_SENSIBLES);
        if (!sensibles.isEmpty()) {
            indicadores.add(new Indicador("I3", "Pide datos sensibles: " + String.join(", ", sensibles) + ".", 20));
        }
        List<String> urgencia = encontrarFrases(texto, textoSinLeet, URGENCIA);
        if (!urgencia.isEmpty()) {
            indicadores.add(new Indicador("I4", "Urgencia o amenaza: " + String.join(", ", urgencia) + ".",
                    Math.min(15, urgencia.size() * 5)));
        }
        Indicador i5 = evaluarEnlaces(enlaces);
        if (i5 != null) indicadores.add(i5);
        if (!encontrarFrases(texto, SALUDO_GENERICO).isEmpty()) {
            indicadores.add(new Indicador("I6", "Saludo genérico: el remitente no sabe tu nombre.", 5));
        }
        List<String> premios = encontrarFrases(texto, textoSinLeet, PREMIOS);
        if (!premios.isEmpty()) {
            indicadores.add(new Indicador("I7", "Promesa de beneficio: " + String.join(", ", premios) + ".", 10));
        }
        List<String> resultadosVT = new ArrayList<>();
        Indicador i8 = consultarVirusTotal(enlaces, resultadosVT);
        if (i8 != null) indicadores.add(i8);

        AnalisisMensaje analisis = new AnalisisMensaje(canal, remitente.trim(), asunto == null ? "" : asunto.trim(),
                enlaces, indicadores, resultadosVT);
        estudiante.agregarEvaluacion(analisis);
        return analisis;
    }

    public AnalisisMensaje analizarEnlace(Estudiante estudiante, String enlace) throws ValidacionException {
        String e = enlace == null ? "" : enlace.trim();
        if (e.isEmpty()) {
            throw new ValidacionException("Pega el enlace que quieres analizar.");
        }
        if (e.contains(" ") || host(e) == null || !host(e).contains(".")) {
            throw new ValidacionException("El enlace no tiene un formato válido (ejemplo: https://sitio.com/pagina).");
        }
        List<String> enlaces = List.of(e);
        List<Indicador> indicadores = new ArrayList<>();
        String h = host(e);
        String parecido = dominioImitado(h);
        if (parecido != null) {
            indicadores.add(new Indicador("I1", "El dominio '" + h + "' imita a '" + parecido + "' (diferencia de "
                    + distanciaLevenshtein(h, parecido) + " caracteres).", 20));
        }
        Indicador i5 = evaluarEnlaces(enlaces);
        if (i5 != null) indicadores.add(i5);
        List<String> resultadosVT = new ArrayList<>();
        Indicador i8 = consultarVirusTotal(enlaces, resultadosVT);
        if (i8 != null) indicadores.add(i8);
        AnalisisMensaje analisis = new AnalisisMensaje(CANAL_SOLO_ENLACE, "No aplica", "", enlaces, indicadores, resultadosVT);
        estudiante.agregarEvaluacion(analisis);
        return analisis;
    }

    private void validar(String canal, String remitente, String cuerpo) throws ValidacionException {
        if (canal == null || canal.isBlank()) {
            throw new ValidacionException("Selecciona el canal por el que recibiste el mensaje.");
        }
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new ValidacionException("El cuerpo del mensaje es obligatorio.");
        }
        if (cuerpo.trim().length() < 20) {
            throw new ValidacionException("El cuerpo debe tener al menos 20 caracteres.");
        }
        if (cuerpo.length() > 5000) {
            throw new ValidacionException("El mensaje excede el máximo de 5000 caracteres.");
        }
        if (remitente != null && remitente.trim().length() > MAX_REMITENTE) {
            throw new ValidacionException("El remitente no puede superar " + MAX_REMITENTE + " caracteres.");
        }
    }

    private Indicador evaluarEnlaces(List<String> enlaces) {
        boolean ip = false, acortador = false, http = false;
        for (String e : enlaces) {
            String h = host(e);
            if (h != null && IP.matcher(h).matches()) ip = true;
            if (h != null && ACORTADORES.contains(h)) acortador = true;
            if (e.toLowerCase(Locale.ROOT).startsWith("http://")) http = true;
        }
        int puntos = (ip ? 10 : 0) + (acortador ? 10 : 0) + (http ? 5 : 0);
        if (puntos == 0) return null;
        List<String> motivos = new ArrayList<>();
        if (ip) motivos.add("usa una dirección IP en lugar de un nombre (+10)");
        if (acortador) motivos.add("usa un acortador que oculta el destino (+10)");
        if (http) motivos.add("usa http sin cifrar (+5)");
        return new Indicador("I5", "Enlaces riesgosos: " + String.join("; ", motivos) + ".", Math.min(15, puntos));
    }

    private Indicador consultarVirusTotal(List<String> enlaces, List<String> resultados) {
        if (enlaces.isEmpty()) return null;
        if (servicioReputacion == null) {
            resultados.add("VirusTotal no está configurado (falta vt.apikey en config.properties). Se muestra solo el análisis local.");
            return null;
        }
        int maximo = 0;
        for (int i = 0; i < enlaces.size() && i < MAX_ENLACES_VIRUSTOTAL; i++) {
            String e = enlaces.get(i);
            try {
                int maliciosos = servicioReputacion.analizarUrl(e);
                maximo = Math.max(maximo, maliciosos);
                resultados.add(e + " -> " + maliciosos + " motor(es) lo marcan como malicioso.");
            } catch (ServicioNoDisponibleException ex) {
                resultados.add(e + " -> No se pudo consultar VirusTotal: " + ex.getMessage());
            }
        }
        if (enlaces.size() > MAX_ENLACES_VIRUSTOTAL) {
            resultados.add("Por el límite de VirusTotal solo se analizaron los primeros " + MAX_ENLACES_VIRUSTOTAL + " enlaces.");
        }
        if (maximo >= 3) return new Indicador("I8", "VirusTotal: " + maximo + " motores marcan un enlace como malicioso.", 40);
        if (maximo >= 1) return new Indicador("I8", "VirusTotal: " + maximo + " motor(es) marcan un enlace como malicioso.", 20);
        return null;
    }

    public static String normalizar(String texto) {
        String t = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return t.replaceAll("\\p{M}", "").replaceAll("\\s+", " ").trim();
    }

    public static String quitarLeetspeak(String texto) {
        return texto.replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a')
                .replace('5', 's').replace('7', 't').replace('$', 's');
    }

    public static List<String> encontrarFrases(String texto, List<String> frases) {
        List<String> encontradas = new ArrayList<>();
        for (String frase : frases) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(frase) + "\\b");
            if (p.matcher(texto).find()) encontradas.add(frase);
        }
        return encontradas;
    }

    private static List<String> encontrarFrases(String texto, String textoSinLeet, List<String> frases) {
        Set<String> union = new LinkedHashSet<>(encontrarFrases(texto, frases));
        union.addAll(encontrarFrases(textoSinLeet, frases));
        return new ArrayList<>(union);
    }

    public static List<String> extraerEnlaces(String cuerpo, List<String> manuales) {
        Set<String> enlaces = new LinkedHashSet<>();
        Matcher m = ENLACE.matcher(cuerpo);
        while (m.find()) {
            enlaces.add(m.group().replaceAll("[.,;:)\\]]+$", ""));
        }
        if (manuales != null) {
            for (String e : manuales) {
                if (!e.isBlank()) enlaces.add(e.trim());
            }
        }
        return new ArrayList<>(enlaces);
    }

    private static String host(String enlace) {
        try {
            String e = enlace.contains("://") ? enlace : "http://" + enlace;
            String h = URI.create(e.replace(" ", "%20")).getHost();
            return h == null ? null : h.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String dominioDeRemitente(String remitente) {
        Matcher m = REMITENTE_CORREO.matcher(remitente == null ? "" : remitente.trim());
        return m.find() ? m.group(1).toLowerCase(Locale.ROOT) : null;
    }

    private static String dominioImitado(String dominio) {
        if (DOMINIOS_LEGITIMOS.contains(dominio)) return null;
        for (String legitimo : DOMINIOS_LEGITIMOS) {
            int d = distanciaLevenshtein(dominio, legitimo);
            if (d >= 1 && d <= 2) return legitimo;
        }
        return null;
    }

    public static int distanciaLevenshtein(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) d[i][0] = i;
        for (int j = 0; j <= b.length(); j++) d[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int costo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + costo);
            }
        }
        return d[a.length()][b.length()];
    }

    public List<AnalisisMensaje> listar(Estudiante estudiante) {
        return estudiante.getEvaluaciones(AnalisisMensaje.class);
    }

    public AnalisisMensaje buscar(Estudiante estudiante, String id) throws ValidacionException {
        for (AnalisisMensaje a : listar(estudiante)) {
            if (a.getId().equalsIgnoreCase(id.trim())) return a;
        }
        throw new ValidacionException("No existe un análisis con el código " + id + ".");
    }

    public void eliminar(Estudiante estudiante, String id) throws ValidacionException {
        estudiante.eliminarEvaluacion(buscar(estudiante, id));
    }
}
