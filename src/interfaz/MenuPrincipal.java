package interfaz;

import negocio.GestorUsuarios;
import negocio.ValidacionException;

public class MenuPrincipal {
    private final ContextoAplicacion ctx;

    public MenuPrincipal(ContextoAplicacion ctx) {
        this.ctx = ctx;
    }

    public void mostrar() {
        GestorUsuarios gu = ctx.getGestorUsuarios();
        while (true) {
            Consola.titulo("ESCUDO DIGITAL - UDLA");
            if (!gu.existeCuenta()) {
                Consola.linea("1. Registrarme con mi correo institucional");
            } else {
                Consola.linea("1. Iniciar sesión");
            }
            Consola.linea("0. Salir");
            if (Consola.leerOpcion(1) == 0) return;
            if (!gu.existeCuenta()) {
                registrar(gu);
            } else {
                iniciarSesion(gu);
            }
        }
    }

    private void registrar(GestorUsuarios gu) {
        Consola.titulo("AVISO DE PRIVACIDAD");
        for (String linea : GestorUsuarios.AVISO_PRIVACIDAD) Consola.linea("  - " + linea);
        boolean acepta = Consola.leerSiNo("¿Aceptas el aviso de privacidad?");
        if (!acepta) {
            Consola.info("Registro cancelado. No se guardó ningún dato.");
            return;
        }
        Consola.titulo("REGISTRO");
        try {
            String nombres = Consola.leerTexto("Nombres y apellidos");
            String correo = Consola.leerTexto("Correo institucional (@udla.edu.ec)");
            String carrera = Consola.leerTexto("Carrera");
            int semestre = Consola.leerEntero("Semestre", GestorUsuarios.SEMESTRE_MIN, GestorUsuarios.SEMESTRE_MAX);
            String clave = gu.registrar(nombres, correo, carrera, semestre, true);
            ctx.guardar();
            Consola.exito("Cuenta creada. Tu contraseña es: " + clave);
            Consola.info("Anótala ahora: no se volverá a mostrar y no se puede cambiar.");
            Consola.info("Ahora inicia sesión con tu correo y esta contraseña.");
        } catch (ValidacionException e) {
            Consola.error(e.getMessage());
        }
    }

    private void iniciarSesion(GestorUsuarios gu) {
        Consola.titulo("INICIAR SESIÓN");
        String correo = Consola.leerTexto("Correo institucional");
        char[] clave = Consola.leerContrasena("Contraseña");
        try {
            Consola.exito("¡Bienvenido, " + gu.iniciarSesion(correo, clave).getNombres() + "!");
            new MenuEstudiante(ctx).mostrar();
        } catch (ValidacionException e) {
            Consola.error(e.getMessage());
        } finally {
            gu.cerrarSesion();
            ctx.guardar();
        }
    }
}
