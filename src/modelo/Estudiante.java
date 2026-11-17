package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Estudiante extends Usuario {
    private static final long serialVersionUID = 1L;

    private String carrera;
    private int semestre;
    private final List<Evaluacion> evaluaciones;

    public Estudiante(String nombres, String correo, String carrera, int semestre,
                      String hashContrasena, String sal) {
        super(nombres, correo, hashContrasena, sal);
        this.carrera = carrera;
        this.semestre = semestre;
        this.evaluaciones = new ArrayList<>();
    }

    @Override
    public String getRol() {
        return "ESTUDIANTE";
    }

    public void agregarEvaluacion(Evaluacion evaluacion) {
        evaluaciones.add(evaluacion);
    }

    public int limpiarHistorial() {
        int n = evaluaciones.size();
        evaluaciones.clear();
        return n;
    }

    public boolean eliminarEvaluacion(Evaluacion evaluacion) {
        return evaluaciones.remove(evaluacion);
    }

    public <T extends Evaluacion> List<T> getEvaluaciones(Class<T> tipo) {
        List<T> resultado = new ArrayList<>();
        for (Evaluacion e : evaluaciones) {
            if (tipo.isInstance(e)) {
                resultado.add(tipo.cast(e));
            }
        }
        return resultado;
    }

    public <T extends Evaluacion> T getUltimaEvaluacion(Class<T> tipo) {
        List<T> lista = getEvaluaciones(tipo);
        return lista.isEmpty() ? null : lista.get(lista.size() - 1);
    }

    public Evaluacion buscarEvaluacion(String id) {
        for (Evaluacion e : evaluaciones) {
            if (e.getId().equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }

    public List<Evaluacion> getTodasLasEvaluaciones() {
        return Collections.unmodifiableList(evaluaciones);
    }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }

    @Override
    public String toString() {
        return String.format("%-28s %-30s %-22s semestre %d", getNombres(), getCorreo(), carrera, semestre);
    }
}
