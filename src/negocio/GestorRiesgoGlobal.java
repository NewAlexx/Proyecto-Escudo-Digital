package negocio;

import java.util.ArrayList;
import java.util.List;
import modelo.ConsultaCorreo;
import modelo.DiagnosticoHigiene;
import modelo.Dispositivo;
import modelo.Estudiante;
import modelo.EvaluacionContrasena;
import modelo.EvaluacionGlobal;

public class GestorRiesgoGlobal {
    public static final double VALOR_NEUTRO = 50;

    private final GestorEntrenamiento gestorEntrenamiento;

    public GestorRiesgoGlobal(GestorEntrenamiento gestorEntrenamiento) {
        this.gestorEntrenamiento = gestorEntrenamiento;
    }

    public EvaluacionGlobal calcularIndice(Estudiante estudiante) throws ValidacionException {
        if (estudiante == null) {
            throw new ValidacionException("Debes iniciar sesión para calcular tu índice.");
        }
        List<String> faltantes = new ArrayList<>();
        double[] r = new double[5];

        DiagnosticoHigiene d = estudiante.getUltimaEvaluacion(DiagnosticoHigiene.class);
        if (d != null) r[0] = 100 - d.getIHD(); else { r[0] = VALOR_NEUTRO; faltantes.add("diagnóstico de higiene"); }

        List<EvaluacionContrasena> claves = estudiante.getEvaluaciones(EvaluacionContrasena.class);
        boolean comprometida = false;
        if (!claves.isEmpty()) {
            double suma = 0;
            for (EvaluacionContrasena c : claves) {
                suma += c.getPuntaje();
                comprometida |= c.estaComprometida();
            }
            r[1] = Math.min(100, 100 - suma / claves.size() + (comprometida ? 20 : 0));
        } else { r[1] = VALOR_NEUTRO; faltantes.add("evaluaciones de contraseñas"); }

        ConsultaCorreo cc = estudiante.getUltimaEvaluacion(ConsultaCorreo.class);
        if (cc != null) r[2] = cc.getPuntaje(); else { r[2] = VALOR_NEUTRO; faltantes.add("consultas de correo"); }

        List<Dispositivo> dispositivos = estudiante.getEvaluaciones(Dispositivo.class);
        boolean sinSoporte = false;
        if (!dispositivos.isEmpty()) {
            double max = 0, suma = 0;
            for (Dispositivo x : dispositivos) {
                max = Math.max(max, x.getPuntaje());
                suma += x.getPuntaje();
                sinSoporte |= Dispositivo.esSistemaSinSoporte(x.getSistemaOperativo());
            }
            r[3] = 0.6 * max + 0.4 * (suma / dispositivos.size());
        } else { r[3] = VALOR_NEUTRO; faltantes.add("dispositivos"); }

        double ir = gestorEntrenamiento.calcularResiliencia(estudiante);
        if (ir >= 0) r[4] = 100 - ir; else { r[4] = VALOR_NEUTRO; faltantes.add("sesiones de entrenamiento"); }

        String escalamiento = null;
        if (comprometida) escalamiento = "tienes una contraseña comprometida";
        else if (sinSoporte) escalamiento = "tienes un dispositivo sin soporte de seguridad";

        EvaluacionGlobal nueva = new EvaluacionGlobal(r, faltantes, escalamiento);
        EvaluacionGlobal anterior = estudiante.getUltimaEvaluacion(EvaluacionGlobal.class);
        if (anterior != null) {
            double delta = nueva.getPuntaje() - anterior.getPuntaje();
            String t = delta <= -5 ? "Mejorando" : (delta >= 5 ? "Empeorando" : "Estable");
            nueva.setTendencia(String.format("%s (%+.2f puntos)", t, delta));
            if (anterior.getFecha().toLocalDate().equals(nueva.getFecha().toLocalDate())) {
                estudiante.eliminarEvaluacion(anterior);
            }
        }
        estudiante.agregarEvaluacion(nueva);
        return nueva;
    }

    public List<EvaluacionGlobal> listar(Estudiante estudiante) {
        return estudiante.getEvaluaciones(EvaluacionGlobal.class);
    }
}
