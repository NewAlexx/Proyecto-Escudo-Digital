package modelo;

public enum FrecuenciaWifi {
    NUNCA(1, "Nunca", 0),
    CASI_NUNCA(2, "Casi nunca", 3),
    A_VECES(3, "A veces", 5),
    CASI_SIEMPRE(4, "Casi siempre", 7),
    SIEMPRE(5, "Siempre", 10);

    public static final int ESCALA_MIN = 1;
    public static final int ESCALA_MAX = 5;

    private final int valor;
    private final String nombre;
    private final int puntos;

    FrecuenciaWifi(int valor, String nombre, int puntos) {
        this.valor = valor;
        this.nombre = nombre;
        this.puntos = puntos;
    }

    public static FrecuenciaWifi desdeEscala(int valor) {
        for (FrecuenciaWifi f : values()) {
            if (f.valor == valor) return f;
        }
        throw new IllegalArgumentException("La respuesta debe estar entre " + ESCALA_MIN + " y " + ESCALA_MAX + ".");
    }

    public int getValor() { return valor; }
    public int getPuntos() { return puntos; }

    @Override
    public String toString() { return nombre; }
}
