package negocio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import modelo.EvaluacionContrasena;

public class CalculadoraFortaleza {
    private static final String ABECEDARIO = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMEROS = "01234567890";
    private static final String SIMBOLOS_TECLADO = ")!@#$%^&*()";

    public EvaluacionContrasena evaluar(char[] clave) {
        int n = clave.length;
        int mayus = 0, minus = 0, digitos = 0, simbolos = 0, medio = 0;
        for (int i = 0; i < n; i++) {
            char c = clave[i];
            if (Character.isUpperCase(c)) mayus++;
            else if (Character.isLowerCase(c)) minus++;
            else if (Character.isDigit(c)) digitos++;
            else simbolos++;
            if (i > 0 && i < n - 1 && (Character.isDigit(c) || !Character.isLetterOrDigit(c))) medio++;
        }
        List<String> detalle = new ArrayList<>();
        List<String> expl = new ArrayList<>();
        int sumas = 0;
        sumas += sumar(detalle, expl, "Número de caracteres (" + n + " × 4)", n * 4,
                "Cada carácter suma 4 puntos. La longitud es lo que más protege: cada carácter extra multiplica "
                + "las combinaciones que un atacante tiene que probar. Con " + n + " caracteres obtienes " + (n * 4) + " puntos.");
        if (mayus > 0) sumas += sumar(detalle, expl, "Mayúsculas ((" + n + " − " + mayus + ") × 2)", (n - mayus) * 2,
                "Tienes " + mayus + " mayúscula(s). Se calcula (largo − mayúsculas) × 2: se premia mezclarlas con otros "
                + "caracteres, porque así el atacante ya no puede probar solo minúsculas.");
        if (minus > 0) sumas += sumar(detalle, expl, "Minúsculas ((" + n + " − " + minus + ") × 2)", (n - minus) * 2,
                "Tienes " + minus + " minúscula(s). Se calcula (largo − minúsculas) × 2: mientras más caracteres de otro "
                + "tipo acompañen a las minúsculas, más puntos.");
        if (digitos > 0 && digitos < n) sumas += sumar(detalle, expl, "Números (" + digitos + " × 4)", digitos * 4,
                "Cada número suma 4 puntos porque agrega 10 opciones más (0 a 9) en cada posición.");
        if (simbolos > 0) sumas += sumar(detalle, expl, "Símbolos (" + simbolos + " × 6)", simbolos * 6,
                "Cada símbolo suma 6 puntos, lo máximo por carácter: hay más de 30 símbolos posibles y casi nadie los usa.");
        if (medio > 0) sumas += sumar(detalle, expl, "Números o símbolos en medio (" + medio + " × 2)", medio * 2,
                "Los números o símbolos dentro de la contraseña (no en el primer ni en el último carácter) suman 2 cada uno. "
                + "Ponerlos al final, como en \"maria123\", es lo primero que prueba un atacante.");
        int requisitos = (n >= 8 ? 1 : 0) + (mayus > 0 ? 1 : 0) + (minus > 0 ? 1 : 0) + (digitos > 0 ? 1 : 0) + (simbolos > 0 ? 1 : 0);
        if (n >= 8 && requisitos >= 4) sumas += sumar(detalle, expl, "Requisitos cumplidos (" + requisitos + " × 2)", requisitos * 2,
                "Cumples " + requisitos + " de los 5 requisitos (8 o más caracteres, mayúscula, minúscula, número y símbolo). "
                + "Con 4 o más, cada requisito suma 2 puntos.");

        int restas = 0;
        if (n > 0 && mayus + minus == n) restas += restar(detalle, expl, "solo letras", n,
                "Tu contraseña tiene solo letras, así que se resta 1 punto por carácter: un ataque de diccionario prueba primero palabras.");
        if (n > 0 && digitos == n) restas += restar(detalle, expl, "solo números", n,
                "Tu contraseña tiene solo números, así que se resta 1 punto por carácter: solo hay 10 opciones por posición.");
        int repetidos = contarRepetidos(clave);
        if (repetidos > 0) restas += restar(detalle, expl, "caracteres repetidos (" + repetidos + ")", repetidos,
                "Hay " + repetidos + " carácter(es) que ya aparecieron antes; cada uno resta 1 punto porque reduce la variedad.");
        int consMayus = 0, consMinus = 0, consNum = 0;
        for (int i = 0; i + 1 < n; i++) {
            if (Character.isUpperCase(clave[i]) && Character.isUpperCase(clave[i + 1])) consMayus++;
            if (Character.isLowerCase(clave[i]) && Character.isLowerCase(clave[i + 1])) consMinus++;
            if (Character.isDigit(clave[i]) && Character.isDigit(clave[i + 1])) consNum++;
        }
        String consec = " Los bloques del mismo tipo (como las letras de una palabra o los dígitos de un año) son fáciles de predecir.";
        if (consMayus > 0) restas += restar(detalle, expl, "mayúsculas consecutivas (" + consMayus + " × 2)", consMayus * 2,
                "Hay " + consMayus + " par(es) de mayúsculas seguidas; cada par resta 2." + consec);
        if (consMinus > 0) restas += restar(detalle, expl, "minúsculas consecutivas (" + consMinus + " × 2)", consMinus * 2,
                "Hay " + consMinus + " par(es) de minúsculas seguidas; cada par resta 2." + consec);
        if (consNum > 0) restas += restar(detalle, expl, "números consecutivos (" + consNum + " × 2)", consNum * 2,
                "Hay " + consNum + " par(es) de números seguidos; cada par resta 2." + consec);
        int seqLetras = contarSecuencias(clave, ABECEDARIO);
        int seqNumeros = contarSecuencias(clave, NUMEROS);
        int seqSimbolos = contarSecuencias(clave, SIMBOLOS_TECLADO);
        String seq = " Las secuencias están en todas las listas que usan los atacantes, por eso restan 3 cada una.";
        if (seqLetras > 0) restas += restar(detalle, expl, "secuencias de letras como abc (" + seqLetras + " × 3)", seqLetras * 3,
                "Hay " + seqLetras + " grupo(s) de 3 letras en orden alfabético (abc, xyz o al revés)." + seq);
        if (seqNumeros > 0) restas += restar(detalle, expl, "secuencias de números como 123 (" + seqNumeros + " × 3)", seqNumeros * 3,
                "Hay " + seqNumeros + " grupo(s) de 3 números en orden (123, 789 o al revés)." + seq);
        if (seqSimbolos > 0) restas += restar(detalle, expl, "secuencias de símbolos como !@# (" + seqSimbolos + " × 3)", seqSimbolos * 3,
                "Hay " + seqSimbolos + " grupo(s) de 3 símbolos seguidos del teclado (!@#)." + seq);

        return new EvaluacionContrasena(n, sumas, restas, detalle, expl);
    }

    private static int sumar(List<String> detalle, List<String> expl, String texto, int valor, String explicacion) {
        detalle.add("+ " + texto + ": +" + valor);
        expl.add(explicacion);
        return valor;
    }

    private static int restar(List<String> detalle, List<String> expl, String texto, int valor, String explicacion) {
        detalle.add("- " + texto + ": −" + valor);
        expl.add(explicacion);
        return valor;
    }

    private static int contarRepetidos(char[] clave) {
        Set<Character> vistos = new HashSet<>();
        int repetidos = 0;
        for (char c : clave) {
            if (!vistos.add(Character.toLowerCase(c))) repetidos++;
        }
        return repetidos;
    }

    private static int contarSecuencias(char[] clave, String referencia) {
        String inversa = new StringBuilder(referencia).reverse().toString();
        int contador = 0;
        for (int i = 0; i + 3 <= clave.length; i++) {
            String tramo = new String(new char[] {
                Character.toLowerCase(clave[i]), Character.toLowerCase(clave[i + 1]), Character.toLowerCase(clave[i + 2])
            });
            if (referencia.contains(tramo) || inversa.contains(tramo)) contador++;
        }
        return contador;
    }
}
