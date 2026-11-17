package modelo;

import java.io.Serializable;

public class Escenario implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int numero;
    private final String texto;
    private final boolean esPhishing;
    private final int dificultad;
    private final String explicacion;

    public Escenario(int numero, String texto, boolean esPhishing, int dificultad, String explicacion) {
        this.numero = numero;
        this.texto = texto;
        this.esPhishing = esPhishing;
        this.dificultad = dificultad;
        this.explicacion = explicacion;
    }

    public int getNumero() { return numero; }
    public String getTexto() { return texto; }
    public boolean esPhishing() { return esPhishing; }
    public int getDificultad() { return dificultad; }
    public String getExplicacion() { return explicacion; }
}
