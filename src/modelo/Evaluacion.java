package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public abstract class Evaluacion implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String id;
    private LocalDateTime fecha;
    protected double puntaje;
    protected String clasificacion;

    protected Evaluacion() {
        this.id = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.fecha = LocalDateTime.now();
    }

    public abstract double calcularPuntaje();

    public abstract String clasificar();

    public abstract List<String> getRecomendaciones();

    public abstract String getTipo();

    public final void evaluar() {
        this.puntaje = redondear(calcularPuntaje());
        this.clasificacion = clasificar();
    }

    protected void actualizarFecha() {
        this.fecha = LocalDateTime.now();
    }

    protected static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public String getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public double getPuntaje() { return puntaje; }
    public String getClasificacion() { return clasificacion; }

    @Override
    public String toString() {
        return String.format("[%s] %-22s %s  %6.2f/100  %s", id, getTipo(), fecha.format(FORMATO_FECHA), puntaje, clasificacion);
    }
}
