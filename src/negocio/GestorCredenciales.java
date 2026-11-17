package negocio;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import modelo.ConsultaCorreo;
import modelo.Estudiante;
import modelo.EvaluacionContrasena;

public class GestorCredenciales {
    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final CalculadoraFortaleza calculadora;
    private final ServicioContrasenasFiltradas servicioContrasenas;
    private final ServicioFiltracionesCorreo servicioCorreo;
    private final ServicioFiltracionesCorreo servicioCorreoRespaldo;

    public GestorCredenciales(ServicioContrasenasFiltradas servicioContrasenas,
                              ServicioFiltracionesCorreo servicioCorreo,
                              ServicioFiltracionesCorreo servicioCorreoRespaldo) {
        this.calculadora = new CalculadoraFortaleza();
        this.servicioContrasenas = servicioContrasenas;
        this.servicioCorreo = servicioCorreo;
        this.servicioCorreoRespaldo = servicioCorreoRespaldo;
    }

    public EvaluacionContrasena evaluarFortaleza(Estudiante estudiante, char[] clave) throws ValidacionException {
        if (clave == null || clave.length == 0) {
            throw new ValidacionException("Ingresa una contraseña para evaluar. No será almacenada.");
        }
        EvaluacionContrasena ev = calculadora.evaluar(clave);
        estudiante.agregarEvaluacion(ev);
        return ev;
    }

    public boolean verificarFiltracion(EvaluacionContrasena ev, char[] clave, boolean autoriza) {
        try {
            if (!autoriza || clave == null || clave.length == 0) {
                return false;
            }
            try {
                ev.registrarFiltracion(servicioContrasenas.contarApariciones(UtilCriptografia.sha1Hex(clave)));
                return true;
            } catch (ServicioNoDisponibleException e) {
                ev.registrarFiltracion(EvaluacionContrasena.SIN_CONSULTA);
                return false;
            }
        } finally {
            if (clave != null) Arrays.fill(clave, '\0');
        }
    }

    public void validarConsultaCorreo(String correo, boolean consentimiento) throws ValidacionException {
        if (correo == null || !CORREO.matcher(correo.trim()).matches()) {
            throw new ValidacionException("El formato del correo electrónico es inválido.");
        }
        if (!consentimiento) {
            throw new ValidacionException("Consulta cancelada: no se dio el consentimiento.");
        }
    }

    public ConsultaCorreo consultarCorreo(Estudiante estudiante, String correo, boolean consentimiento)
            throws ValidacionException, ServicioNoDisponibleException {
        validarConsultaCorreo(correo, consentimiento);
        String c = correo.trim().toLowerCase();
        List<String> filtraciones;
        String fuente;
        try {
            filtraciones = servicioCorreo.buscarFiltraciones(c);
            fuente = servicioCorreo.getNombre();
        } catch (ServicioNoDisponibleException principal) {
            if (servicioCorreoRespaldo == null) throw principal;
            try {
                filtraciones = servicioCorreoRespaldo.buscarFiltraciones(c);
                fuente = servicioCorreoRespaldo.getNombre();
            } catch (ServicioNoDisponibleException respaldo) {
                throw new ServicioNoDisponibleException("No se pudo conectar con el servicio de filtraciones. Intenta más tarde. ("
                        + principal.getMessage() + ")");
            }
        }
        ConsultaCorreo consulta = new ConsultaCorreo(c, filtraciones, fuente);
        estudiante.agregarEvaluacion(consulta);
        return consulta;
    }

    public List<EvaluacionContrasena> listarContrasenas(Estudiante estudiante) {
        return estudiante.getEvaluaciones(EvaluacionContrasena.class);
    }

    public List<ConsultaCorreo> listarCorreos(Estudiante estudiante) {
        return estudiante.getEvaluaciones(ConsultaCorreo.class);
    }
}
