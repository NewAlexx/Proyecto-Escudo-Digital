package interfaz;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Consola {
    private static final Scanner SC = new Scanner(System.in, StandardCharsets.UTF_8);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Consola() { }

    public static String leerTexto(String etiqueta) {
        System.out.print(etiqueta + ": ");
        try {
            return SC.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new FinDeEntradaException();
        }
    }

    public static int leerEntero(String etiqueta, int min, int max) {
        while (true) {
            String t = leerTexto(etiqueta + " (" + min + "-" + max + ")");
            try {
                int n = Integer.parseInt(t);
                if (n >= min && n <= max) return n;
                error("Ingresa un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                error("Los campos numéricos son inválidos.");
            }
        }
    }

    public static int leerOpcion(int max) {
        return leerEntero("Opción", 0, max);
    }

    public static boolean leerSiNo(String etiqueta) {
        while (true) {
            String t = leerTexto(etiqueta + " (S/N)").toUpperCase();
            if (t.equals("S") || t.equals("SI") || t.equals("SÍ")) return true;
            if (t.equals("N") || t.equals("NO")) return false;
            error("Responde S o N.");
        }
    }

    public static char[] leerContrasena(String etiqueta) {
        Console c = System.console();
        if (c != null) {
            char[] clave = c.readPassword(etiqueta + ": ");
            if (clave == null) throw new FinDeEntradaException();
            return clave;
        }
        return leerTexto(etiqueta).toCharArray();
    }

    public static LocalDate leerFecha(String etiqueta) {
        while (true) {
            String t = leerTexto(etiqueta + " (dd/mm/aaaa)");
            try {
                return LocalDate.parse(t, FECHA);
            } catch (DateTimeParseException e) {
                error("Formato de fecha inválido. Usa dd/mm/aaaa, por ejemplo 15/11/2026.");
            }
        }
    }

    public static List<String> leerLista(String etiqueta) {
        List<String> lista = new ArrayList<>();
        for (String parte : leerTexto(etiqueta + " (separados por coma, Enter si no hay)").split(",")) {
            if (!parte.isBlank()) lista.add(parte.trim());
        }
        return lista;
    }

    public static String leerMultilinea(String etiqueta) {
        System.out.println(etiqueta + " (pega el texto y escribe FIN en una línea aparte para terminar):");
        StringBuilder sb = new StringBuilder();
        while (true) {
            String linea;
            try {
                linea = SC.nextLine();
            } catch (NoSuchElementException e) {
                throw new FinDeEntradaException();
            }
            if (linea.trim().equalsIgnoreCase("FIN")) break;
            sb.append(linea).append('\n');
        }
        return sb.toString().trim();
    }

    public static <E extends Enum<E>> E leerEnum(String etiqueta, E[] valores) {
        System.out.println(etiqueta + ":");
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i]);
        }
        return valores[leerEntero("Elige", 1, valores.length) - 1];
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  " + texto);
        System.out.println("=".repeat(70));
    }

    public static void exito(String texto) { System.out.println("[OK] " + texto); }
    public static void error(String texto) { System.out.println("[ERROR] " + texto); }
    public static void info(String texto) { System.out.println("[i] " + texto); }
    public static void linea(String texto) { System.out.println(texto); }

    public static void pausa() {
        leerTexto("Presiona Enter para continuar");
    }

    public static class FinDeEntradaException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
