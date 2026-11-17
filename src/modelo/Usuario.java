package modelo;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_INTENTOS = 3;
    public static final int MINUTOS_BLOQUEO = 5;

    private String nombres;
    private final String correo;
    private final String hashContrasena;
    private final String sal;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    protected Usuario(String nombres, String correo, String hashContrasena, String sal) {
        this.nombres = nombres;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.sal = sal;
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    public abstract String getRol();

    public boolean estaBloqueado() {
        return bloqueadoHasta != null && LocalDateTime.now().isBefore(bloqueadoHasta);
    }

    public long getMinutosRestantesBloqueo() {
        if (!estaBloqueado()) {
            return 0;
        }
        long segundos = Duration.between(LocalDateTime.now(), bloqueadoHasta).getSeconds();
        return Math.max(1, (segundos + 59) / 60);
    }

    public int registrarIntentoFallido() {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
            intentosFallidos = 0;
            return 0;
        }
        return MAX_INTENTOS - intentosFallidos;
    }

    public void reiniciarIntentos() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getCorreo() { return correo; }
    public String getHashContrasena() { return hashContrasena; }
    public String getSal() { return sal; }
    public int getIntentosFallidos() { return intentosFallidos; }

    @Override
    public String toString() {
        return String.format("%-28s %-30s %s", nombres, correo, getRol());
    }
}
