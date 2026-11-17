package negocio;

import java.util.ArrayList;
import java.util.List;
import modelo.DiagnosticoHigiene;
import modelo.Estudiante;
import modelo.Evaluacion;

public class GestorDiagnostico {
    public static final String[] PREGUNTAS = {
        "¿Usas una contraseña diferente en cada página web (correo, redes sociales, aula virtual, banca)?",
        "¿Tienes activada la verificación en dos pasos en tu correo y tu banca?",
        "¿Instalas las actualizaciones del sistema operativo en la primera semana?",
        "¿Mantienes actualizadas tus aplicaciones y tu antivirus?",
        "¿Verificas el remitente y el destino de un enlace antes de hacer clic?",
        "¿Evitas ingresar contraseñas desde enlaces recibidos por correo o mensajería?",
        "¿Respaldas tus trabajos en al menos dos lugares?",
        "¿Tus dispositivos tienen bloqueo de pantalla y cifrado?",
        "¿Evitas entrar a cuentas importantes desde WiFi públicas?",
        "¿Revisas los permisos de las aplicaciones que instalas?"
    };
    public static final String[] ESCALA = {"Nunca", "Casi nunca", "A veces", "Casi siempre", "Siempre"};
    public static final String[] TIPOS_SITIO = {"Correo", "Redes sociales", "Aula virtual", "Banca", "Tiendas en línea", "Otras"};

    public static final int ESCALA_MIN = 1;
    public static final int ESCALA_MAX = 5;

    public static boolean debePreguntarReutilizacion(int respuestaP1) {
        return respuestaP1 <= 3;
    }

    public DiagnosticoHigiene realizarDiagnostico(Estudiante estudiante, int[] respuestas, List<String> sitiosReutilizados)
            throws ValidacionException {
        if (estudiante == null) {
            throw new ValidacionException("Debes iniciar sesión para realizar el diagnóstico.");
        }
        if (respuestas == null || respuestas.length != PREGUNTAS.length) {
            throw new ValidacionException("Debes responder las 10 preguntas antes de continuar.");
        }
        for (int i = 0; i < respuestas.length; i++) {
            if (respuestas[i] < ESCALA_MIN || respuestas[i] > ESCALA_MAX) {
                throw new ValidacionException("La respuesta de la pregunta " + (i + 1) + " debe estar entre " + ESCALA_MIN + " y " + ESCALA_MAX + ".");
            }
        }
        List<String> sitios = debePreguntarReutilizacion(respuestas[0]) ? sitiosReutilizados : new ArrayList<>();
        DiagnosticoHigiene d = new DiagnosticoHigiene(respuestas, sitios);
        estudiante.agregarEvaluacion(d);
        return d;
    }

    public String compararConAnterior(Estudiante estudiante, DiagnosticoHigiene actual) {
        List<DiagnosticoHigiene> lista = estudiante.getEvaluaciones(DiagnosticoHigiene.class);
        int pos = lista.indexOf(actual);
        if (pos <= 0) {
            return "Diagnóstico inicial registrado.";
        }
        DiagnosticoHigiene anterior = lista.get(pos - 1);
        double delta = actual.getIHD() - anterior.getIHD();
        String estado = delta >= 5 ? "Mejora" : (delta <= -5 ? "Retroceso" : "Estable");
        return String.format("Tu índice cambió %+.2f puntos respecto al diagnóstico del %s: %s.",
                delta, anterior.getFecha().format(Evaluacion.FORMATO_FECHA), estado);
    }

    public List<DiagnosticoHigiene> listar(Estudiante estudiante) {
        return estudiante.getEvaluaciones(DiagnosticoHigiene.class);
    }

    public DiagnosticoHigiene buscar(Estudiante estudiante, String id) throws ValidacionException {
        for (DiagnosticoHigiene d : listar(estudiante)) {
            if (d.getId().equalsIgnoreCase(id.trim())) return d;
        }
        throw new ValidacionException("No existe un diagnóstico con el código " + id + ".");
    }

    public void eliminar(Estudiante estudiante, String id) throws ValidacionException {
        estudiante.eliminarEvaluacion(buscar(estudiante, id));
    }
}
