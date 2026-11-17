package negocio;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class UtilCriptografia {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERACIONES = 65_536;
    private static final int LONGITUD_BITS = 256;
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final String SIMBOLOS = "!@#$%&*?-_+=";

    private UtilCriptografia() { }

    public static String generarSal() {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    public static String hashPBKDF2(char[] contrasena, String salBase64) {
        PBEKeySpec spec = new PBEKeySpec(contrasena, Base64.getDecoder().decode(salBase64), ITERACIONES, LONGITUD_BITS);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no está disponible en esta versión de Java", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verificar(char[] contrasena, String salBase64, String hashEsperado) {
        String calculado = hashPBKDF2(contrasena, salBase64);
        return MessageDigest.isEqual(calculado.getBytes(StandardCharsets.UTF_8), hashEsperado.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha1Hex(char[] texto) {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(texto));
        byte[] bytes = Arrays.copyOf(buffer.array(), buffer.limit());
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(bytes);
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 no está disponible", e);
        } finally {
            Arrays.fill(bytes, (byte) 0);
            Arrays.fill(buffer.array(), (byte) 0);
        }
    }

    public static String generarContrasena() {
        List<Character> caracteres = new ArrayList<>();
        caracteres.add(aleatorio(MAYUSCULAS));
        caracteres.add(aleatorio(MINUSCULAS));
        caracteres.add(aleatorio(DIGITOS));
        caracteres.add(aleatorio(SIMBOLOS));
        String todos = MAYUSCULAS + MINUSCULAS + DIGITOS + SIMBOLOS;
        while (caracteres.size() < 12) {
            caracteres.add(aleatorio(todos));
        }
        Collections.shuffle(caracteres, RANDOM);
        StringBuilder sb = new StringBuilder();
        for (char c : caracteres) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static char aleatorio(String alfabeto) {
        return alfabeto.charAt(RANDOM.nextInt(alfabeto.length()));
    }
}
