package modelo;

import java.io.Serializable;

public class Indicador implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String descripcion;
    private final int peso;

    public Indicador(String codigo, String descripcion, int peso) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.peso = peso;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public int getPeso() { return peso; }

    @Override
    public String toString() {
        return String.format("%-3s +%2d  %s", codigo, peso, descripcion);
    }
}
