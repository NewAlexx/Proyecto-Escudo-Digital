package interfaz;

import modelo.Estudiante;
import negocio.GestorUsuarios;
import negocio.ValidacionException;

public class MenuEstudiante {
    private final ContextoAplicacion ctx;

    public MenuEstudiante(ContextoAplicacion ctx) {
        this.ctx = ctx;
    }

    public void mostrar() throws ValidacionException {
        GestorUsuarios gu = ctx.getGestorUsuarios();
        while (gu.existeCuenta()) {
            Estudiante e = gu.getEstudianteActivo();
            Consola.titulo("BIENVENIDO: " + e.getNombres());
            Consola.linea("--- Módulo 1: Acceso seguro y diagnóstico");
            Consola.linea("1. Diagnóstico de higiene digital");
            Consola.linea("--- Módulo 2: Credenciales y exposición");
            Consola.linea("2. Contraseñas y correos filtrados");
            Consola.linea("3. Mis dispositivos");
            Consola.linea("--- Módulo 3: Ingeniería social y riesgo global");
            Consola.linea("4. Analizar un mensaje sospechoso");
            Consola.linea("5. Entrenamiento antiphishing");
            Consola.linea("6. Índice global de riesgo digital");
            Consola.linea("--- Mi cuenta");
            Consola.linea("7. Mi perfil");
            Consola.linea("8. Limpiar mi historial");
            Consola.linea("0. Cerrar sesión");
            switch (Consola.leerOpcion(8)) {
                case 0: return;
                case 1: new MenuDiagnostico(ctx, e).mostrar(); break;
                case 2: new MenuCredenciales(ctx, e).mostrar(); break;
                case 3: new MenuDispositivos(ctx, e).mostrar(); break;
                case 4: new MenuPhishing(ctx, e).mostrar(); break;
                case 5: new MenuEntrenamiento(ctx, e).mostrar(); break;
                case 6: new MenuRiesgo(ctx, e).mostrar(); break;
                case 7: perfil(gu); break;
                case 8: limpiarHistorial(gu); break;
                default: break;
            }
            ctx.guardar();
        }
    }

    private void perfil(GestorUsuarios gu) {
        try {
            Estudiante e = gu.consultarPerfil();
            Consola.titulo("MI PERFIL");
            Consola.linea("Nombres:  " + e.getNombres());
            Consola.linea("Correo:   " + e.getCorreo());
            Consola.linea("Carrera:  " + e.getCarrera());
            Consola.linea("Semestre: " + e.getSemestre());
            Consola.linea("1. Actualizar mis datos");
            Consola.linea("2. Eliminar mi cuenta y todos mis datos");
            Consola.linea("0. Volver");
            int op = Consola.leerOpcion(2);
            if (op == 1) {
                Consola.info("Deja vacío para mantener el valor actual. El correo y la contraseña no se pueden cambiar.");
                String nombres = Consola.leerTexto("Nombres [" + e.getNombres() + "]");
                String carrera = Consola.leerTexto("Carrera [" + e.getCarrera() + "]");
                String sem = Consola.leerTexto("Semestre (1-12) [" + e.getSemestre() + "]");
                int semestre;
                try {
                    semestre = sem.isBlank() ? e.getSemestre() : Integer.parseInt(sem.trim());
                } catch (NumberFormatException ex) {
                    throw new ValidacionException("Los campos numéricos son inválidos.");
                }
                gu.actualizarPerfil(nombres.isBlank() ? e.getNombres() : nombres,
                        carrera.isBlank() ? e.getCarrera() : carrera, semestre);
                Consola.exito("Datos actualizados.");
            } else if (op == 2) {
                if (Consola.leerSiNo("Se borrarán tu cuenta, todos tus resultados y el archivo de datos del equipo. ¿Confirmas?")) {
                    gu.eliminarCuenta();
                    Consola.exito("Cuenta y datos eliminados de este equipo. Queda libre para un nuevo registro.");
                }
            }
        } catch (ValidacionException ex) {
            Consola.error(ex.getMessage());
        }
    }

    private void limpiarHistorial(GestorUsuarios gu) {
        try {
            if (Consola.leerSiNo("Se borrarán los resultados de todos los módulos (tu cuenta se mantiene). ¿Confirmas?")) {
                int n = gu.limpiarHistorial();
                Consola.exito("Historial limpio. Se borraron " + n + " resultados.");
            }
        } catch (ValidacionException ex) {
            Consola.error(ex.getMessage());
        }
    }
}
