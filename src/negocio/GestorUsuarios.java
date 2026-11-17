package negocio;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import modelo.Estudiante;
import modelo.Usuario;

public class GestorUsuarios implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern CORREO_UDLA = Pattern.compile("^[a-z0-9._%+-]+@udla\\.edu\\.ec$");
    public static final int SEMESTRE_MIN = 1;
    public static final int SEMESTRE_MAX = 12;

    public static final String[] AVISO_PRIVACIDAD = {
        "Escudo Digital funciona solo en este equipo y no usa servidores ni bases de datos externas.",
        "Tus datos y resultados se guardan únicamente en este equipo (archivo escudo.dat) hasta que TÚ decidas borrarlos.",
        "Tu contraseña de acceso se guarda solo como hash PBKDF2 con sal; nadie puede leerla.",
        "Al evaluar una contraseña, NO se guarda. Solo si lo autorizas, se envían a Pwned Passwords los 5 primeros caracteres de su hash SHA-1.",
        "Al consultar un correo, se envía a XposedOrNot (o a LeakCheck si el primero no responde), solo si das tu consentimiento.",
        "Al analizar un mensaje, solo los enlaces que pegues se envían a VirusTotal; el texto del mensaje no sale del equipo.",
        "Puedes borrar tus resultados con \"Limpiar mi historial\" o todo (cuenta y archivo) con \"Eliminar mi cuenta\"."
    };

    private Estudiante cuenta;
    private transient Usuario usuarioActivo;
    private int intentosCorreoDesconocido;

    public boolean existeCuenta() {
        return cuenta != null;
    }

    public String registrar(String nombres, String correo, String carrera, int semestre, boolean aceptaAviso)
            throws ValidacionException {
        if (existeCuenta()) {
            throw new ValidacionException("Ya existe una cuenta en este equipo. Solo se permite un usuario por equipo.");
        }
        if (!aceptaAviso) {
            throw new ValidacionException("Debes aceptar el aviso de privacidad para registrarte.");
        }
        if (estaVacio(nombres) || estaVacio(correo) || estaVacio(carrera)) {
            throw new ValidacionException("Todos los campos son obligatorios.");
        }
        String c = normalizarCorreo(correo);
        if (!CORREO_UDLA.matcher(c).matches()) {
            throw new ValidacionException("Debe usar un correo institucional (@udla.edu.ec).");
        }
        validarSemestre(semestre);
        String contrasena = UtilCriptografia.generarContrasena();
        String sal = UtilCriptografia.generarSal();
        String hash = UtilCriptografia.hashPBKDF2(contrasena.toCharArray(), sal);
        cuenta = new Estudiante(nombres.trim(), c, carrera.trim(), semestre, hash, sal);
        intentosCorreoDesconocido = 0;
        return contrasena;
    }

    public Estudiante consultarPerfil() throws ValidacionException {
        return getEstudianteActivo();
    }

    public void actualizarPerfil(String nombres, String carrera, int semestre) throws ValidacionException {
        Estudiante e = getEstudianteActivo();
        if (estaVacio(nombres) || estaVacio(carrera)) {
            throw new ValidacionException("Todos los campos son obligatorios.");
        }
        validarSemestre(semestre);
        e.setNombres(nombres.trim());
        e.setCarrera(carrera.trim());
        e.setSemestre(semestre);
    }

    public void eliminarCuenta() throws ValidacionException {
        getEstudianteActivo().limpiarHistorial();
        cuenta = null;
        usuarioActivo = null;
    }

    public int limpiarHistorial() throws ValidacionException {
        return getEstudianteActivo().limpiarHistorial();
    }

    public Usuario iniciarSesion(String correo, char[] contrasena) throws ValidacionException {
        try {
            if (estaVacio(correo) || contrasena == null || contrasena.length == 0) {
                throw new ValidacionException("Correo y contraseña son obligatorios.");
            }
            String c = normalizarCorreo(correo);
            if (cuenta == null || !cuenta.getCorreo().equals(c)) {
                throw new ValidacionException(fallaCorreoDesconocido());
            }
            if (cuenta.estaBloqueado()) {
                throw new ValidacionException("Cuenta bloqueada por intentos fallidos. Intenta nuevamente en "
                        + cuenta.getMinutosRestantesBloqueo() + " minutos.");
            }
            if (!UtilCriptografia.verificar(contrasena, cuenta.getSal(), cuenta.getHashContrasena())) {
                int restantes = cuenta.registrarIntentoFallido();
                if (restantes == 0) {
                    throw new ValidacionException("Cuenta bloqueada por intentos fallidos. Intenta nuevamente en "
                            + Usuario.MINUTOS_BLOQUEO + " minutos.");
                }
                throw new ValidacionException("Correo o contraseña incorrectos. Intentos restantes: " + restantes + ".");
            }
            cuenta.reiniciarIntentos();
            usuarioActivo = cuenta;
            return cuenta;
        } finally {
            if (contrasena != null) Arrays.fill(contrasena, '\0');
        }
    }

    private String fallaCorreoDesconocido() {
        intentosCorreoDesconocido++;
        if (intentosCorreoDesconocido >= Usuario.MAX_INTENTOS) {
            intentosCorreoDesconocido = 0;
            return "Cuenta bloqueada por intentos fallidos. Intenta nuevamente en " + Usuario.MINUTOS_BLOQUEO + " minutos.";
        }
        return "Correo o contraseña incorrectos. Intentos restantes: " + (Usuario.MAX_INTENTOS - intentosCorreoDesconocido) + ".";
    }

    public void cerrarSesion() {
        usuarioActivo = null;
    }

    public Estudiante getEstudianteActivo() throws ValidacionException {
        if (!(usuarioActivo instanceof Estudiante)) {
            throw new ValidacionException("No hay un usuario con sesión activa.");
        }
        return (Estudiante) usuarioActivo;
    }

    private void validarSemestre(int semestre) throws ValidacionException {
        if (semestre < SEMESTRE_MIN || semestre > SEMESTRE_MAX) {
            throw new ValidacionException("El semestre debe ser un número entre " + SEMESTRE_MIN + " y " + SEMESTRE_MAX + ".");
        }
    }

    private static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
