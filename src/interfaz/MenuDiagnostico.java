package interfaz;

import java.util.ArrayList;
import java.util.List;
import modelo.DiagnosticoHigiene;
import modelo.Estudiante;
import negocio.GestorDiagnostico;
import negocio.ValidacionException;

public class MenuDiagnostico {
    private final ContextoAplicacion ctx;
    private final Estudiante est;
    private final GestorDiagnostico gestor;

    public MenuDiagnostico(ContextoAplicacion ctx, Estudiante est) {
        this.ctx = ctx;
        this.est = est;
        this.gestor = ctx.getGestorDiagnostico();
    }

    public void mostrar() {
        while (true) {
            Consola.titulo("DIAGNÓSTICO DE HIGIENE DIGITAL");
            Consola.linea("1. Realizar diagnóstico");
            Consola.linea("2. Ver historial");
            Consola.linea("3. Ver detalle de un diagnóstico");
            Consola.linea("4. Eliminar un diagnóstico");
            Consola.linea("0. Volver");
            int op = Consola.leerOpcion(4);
            if (op == 0) return;
            try {
                switch (op) {
                    case 1: realizar(); break;
                    case 2: historial(); break;
                    case 3: mostrarDetalle(gestor.buscar(est, Consola.leerTexto("Código"))); break;
                    case 4: gestor.eliminar(est, Consola.leerTexto("Código")); Consola.exito("Diagnóstico eliminado."); break;
                    default: break;
                }
            } catch (ValidacionException e) {
                Consola.error(e.getMessage());
            }
        }
    }

    private void realizar() throws ValidacionException {
        Consola.info("Escala: 1 = Nunca, 2 = Casi nunca, 3 = A veces, 4 = Casi siempre, 5 = Siempre.");
        int[] r = new int[GestorDiagnostico.PREGUNTAS.length];
        List<String> sitios = new ArrayList<>();
        for (int i = 0; i < r.length; i++) {
            Consola.linea("P" + (i + 1) + ". " + GestorDiagnostico.PREGUNTAS[i]);
            r[i] = Consola.leerEntero("Respuesta", GestorDiagnostico.ESCALA_MIN, GestorDiagnostico.ESCALA_MAX);
            if (i == 0 && GestorDiagnostico.debePreguntarReutilizacion(r[0])) {
                Consola.linea("¿En qué tipos de páginas repites la misma contraseña?");
                for (int k = 0; k < GestorDiagnostico.TIPOS_SITIO.length; k++) {
                    Consola.linea("  " + (k + 1) + ". " + GestorDiagnostico.TIPOS_SITIO[k]);
                }
                for (String n : Consola.leerLista("Números de las opciones")) {
                    try {
                        int idx = Integer.parseInt(n) - 1;
                        if (idx >= 0 && idx < GestorDiagnostico.TIPOS_SITIO.length && !sitios.contains(GestorDiagnostico.TIPOS_SITIO[idx])) {
                            sitios.add(GestorDiagnostico.TIPOS_SITIO[idx]);
                        }
                    } catch (NumberFormatException ignorado) {
                        Consola.error("Opción ignorada: " + n);
                    }
                }
            }
        }
        DiagnosticoHigiene d = gestor.realizarDiagnostico(est, r, sitios);
        Consola.exito(String.format("Diagnóstico completado. Índice de Higiene Digital: %.2f/100 (%s).", d.getIHD(), d.getClasificacion()));
        if (!d.getDimensionesPrioritarias().isEmpty()) {
            Consola.info("Tus dimensiones prioritarias son: " + String.join(", ", d.getDimensionesPrioritarias()) + ".");
        }
        Consola.info(gestor.compararConAnterior(est, d));
        mostrarDetalle(d);
    }

    private void historial() {
        List<DiagnosticoHigiene> lista = gestor.listar(est);
        if (lista.isEmpty()) Consola.info("Aún no tienes diagnósticos.");
        for (DiagnosticoHigiene d : lista) Consola.linea(d.toString());
    }

    private void mostrarDetalle(DiagnosticoHigiene d) {
        Consola.linea("Puntaje por dimensión:");
        double[] pd = d.getPuntajesDimension();
        for (int i = 0; i < pd.length; i++) {
            Consola.linea(String.format("  %-24s %6.2f  (peso %.2f)", DiagnosticoHigiene.DIMENSIONES[i], pd[i], DiagnosticoHigiene.PESOS[i]));
        }
        Consola.linea("Recomendaciones:");
        for (String rec : d.getRecomendaciones()) Consola.linea("  - " + rec);
    }
}
