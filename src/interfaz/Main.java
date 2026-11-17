package interfaz;

public class Main {
    public static void main(String[] args) {
        ContextoAplicacion contexto = new ContextoAplicacion();
        try {
            new MenuPrincipal(contexto).mostrar();
        } catch (Consola.FinDeEntradaException e) {
            Consola.linea("\nFin de la entrada.");
        } finally {
            contexto.guardar();
            Consola.linea("Tus datos quedaron guardados en este equipo. Puedes borrarlos cuando quieras desde \"Mi cuenta\". ¡Hasta pronto!");
        }
    }
}
