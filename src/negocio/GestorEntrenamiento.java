package negocio;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import modelo.DiagnosticoHigiene;
import modelo.Escenario;
import modelo.Estudiante;
import modelo.SesionEntrenamiento;

public class GestorEntrenamiento {
    public static final String[][] TUTORIAL = {
        {"1. Revisa el dominio del remitente",
         "Mira la dirección completa, no solo el nombre que aparece. Los atacantes cambian una letra: soporte@udla-edu.ec "
         + "o soporte@udIa.edu.ec (con I mayúscula) en lugar de @udla.edu.ec. Un banco nunca te escribe desde @gmail.com."},
        {"2. Pasa el mouse sobre el enlace SIN hacer clic",
         "En la computadora, deja el puntero encima del enlace: abajo a la izquierda del navegador o en un recuadro "
         + "aparece la dirección real. En el celular, mantén presionado el enlace y lee la dirección sin abrirla."},
        {"3. Desconfía del enlace camuflado",
         "El texto azul puede decir https://www.udla.edu.ec/becas y llevar en realidad a http://udla-becas.xyz/login. "
         + "También puede esconderse detrás de una imagen o un botón (\"Ver más\"). Si el texto y la dirección real no "
         + "coinciden, es phishing."},
        {"4. Fíjate en la urgencia y las amenazas",
         "Frases como \"tu cuenta será bloqueada en 24 horas\" o \"último aviso\" buscan que actúes sin pensar."},
        {"5. Nadie legítimo te pide tu contraseña",
         "Ni la universidad ni tu banco te piden contraseñas, PIN o códigos de verificación por correo o mensaje."},
        {"6. Premios y deudas que no esperabas",
         "Si no participaste en un sorteo o no tienes cuenta en ese banco, el premio o la deuda son falsos."},
        {"7. Saludos genéricos",
         "\"Estimado usuario\" o \"Querido cliente\" en lugar de tu nombre es una señal de envío masivo."},
        {"8. Adjuntos peligrosos",
         "No abras archivos .exe, .scr, .js, .zip inesperados ni dobles extensiones como factura.pdf.exe."},
        {"9. Ante la duda, entra por tu cuenta",
         "No uses el enlace: escribe tú mismo la dirección oficial en el navegador o llama a la institución. "
         + "Luego borra el mensaje o repórtalo como phishing."}
    };

    public static final int MAX_SESIONES_DIARIAS = 3;

    private final List<Escenario> banco;
    private final Random random;

    public GestorEntrenamiento() {
        this.banco = crearBanco();
        this.random = new SecureRandom();
    }

    public int calcularNivelInicial(Estudiante estudiante) {
        DiagnosticoHigiene d = estudiante.getUltimaEvaluacion(DiagnosticoHigiene.class);
        if (d == null) return 1;
        switch (d.getClasificacion()) {
            case "Robusto": return 3;
            case "Aceptable": return 2;
            default: return 1;
        }
    }

    public SesionEntrenamiento iniciarSesion(Estudiante estudiante) throws ValidacionException {
        if (sesionesDeHoy(estudiante) >= MAX_SESIONES_DIARIAS) {
            throw new ValidacionException("Alcanzaste el límite de " + MAX_SESIONES_DIARIAS + " cuestionarios por hoy. Vuelve mañana.");
        }
        return new SesionEntrenamiento(calcularNivelInicial(estudiante));
    }

    public int sesionesDeHoy(Estudiante estudiante) {
        int n = 0;
        for (SesionEntrenamiento s : estudiante.getEvaluaciones(SesionEntrenamiento.class)) {
            if (s.getFecha().toLocalDate().equals(LocalDate.now())) n++;
        }
        return n;
    }

    public Escenario siguienteEscenario(SesionEntrenamiento sesion) {
        for (int distancia = 0; distancia <= 2; distancia++) {
            List<Escenario> candidatos = new ArrayList<>();
            for (Escenario e : banco) {
                if (!sesion.yaPresentado(e) && Math.abs(e.getDificultad() - sesion.getNivelActual()) == distancia) {
                    candidatos.add(e);
                }
            }
            if (!candidatos.isEmpty()) {
                return candidatos.get(random.nextInt(candidatos.size()));
            }
        }
        throw new IllegalStateException("No quedan escenarios disponibles.");
    }

    public boolean responder(SesionEntrenamiento sesion, Escenario escenario, boolean marcoPhishing) {
        return sesion.registrarRespuesta(escenario, marcoPhishing);
    }

    public void finalizar(Estudiante estudiante, SesionEntrenamiento sesion) throws ValidacionException {
        if (!sesion.estaCompleta()) {
            throw new ValidacionException("El cuestionario fue abandonado y no se registrará en tu historial.");
        }
        sesion.evaluar();
        estudiante.agregarEvaluacion(sesion);
    }

    public List<SesionEntrenamiento> listar(Estudiante estudiante) {
        return estudiante.getEvaluaciones(SesionEntrenamiento.class);
    }

    public double calcularResiliencia(Estudiante estudiante) {
        List<SesionEntrenamiento> sesiones = listar(estudiante);
        double[] pesos = {0.5, 0.3, 0.2};
        double suma = 0, sumaPesos = 0;
        for (int k = 0; k < 3 && k < sesiones.size(); k++) {
            SesionEntrenamiento s = sesiones.get(sesiones.size() - 1 - k);
            suma += pesos[k] * s.getPuntaje();
            sumaPesos += pesos[k];
        }
        return sumaPesos == 0 ? -1 : Math.round(suma / sumaPesos * 100.0) / 100.0;
    }

    public static String nivelResiliencia(double ir) {
        if (ir >= 85) return "Experto";
        if (ir >= 70) return "Competente";
        if (ir >= 50) return "En formación";
        return "Vulnerable";
    }

    private static List<Escenario> crearBanco() {
        List<Escenario> b = new ArrayList<>();
        b.add(new Escenario(1, "De: premios-udla@gmail.com\nAsunto: ¡¡GANASTE UNA LAPTOP!!\nFelicidades, fuiste seleccionado. Envía tu cédula y número de tarjeta para el envío.",
                true, 1, "Premio inesperado, correo gratuito que dice ser la UDLA y pide cédula y tarjeta."));
        b.add(new Escenario(2, "De: soporte@udla-edu.ec\nAsunto: URGENTE: cuenta bloqueada\nSu cuenta será eliminada en 24 horas. Ingrese su contraseña aquí: http://bit.ly/udla-login",
                true, 1, "Dominio que imita a udla.edu.ec (guion en lugar de punto), urgencia, acortador y pedido de contraseña."));
        b.add(new Escenario(3, "SMS de +593 99 123 4567:\nBanco Andino: su tarjeta fue bloqueada. Confirme sus datos en http://45.12.88.10/banco",
                true, 1, "El enlace usa una dirección IP y http sin cifrar; los bancos no piden confirmar datos por SMS."));
        b.add(new Escenario(4, "De: rrhh.empleos@hotmail.com\nAsunto: Trabajo remoto\nGana $3.000 semanales sin experiencia. Descarga el formulario adjunto: formulario.pdf.exe",
                true, 1, "Oferta demasiado buena y adjunto con doble extensión (.pdf.exe): es un programa disfrazado de PDF."));
        b.add(new Escenario(5, "De: biblioteca@udla.edu.ec\nAsunto: Recordatorio de devolución\nHola Andrea, el libro 'Cálculo' vence el viernes. Puedes renovarlo en el portal de la biblioteca.",
                false, 1, "Dominio oficial, te llama por tu nombre y no pide datos ni contiene enlaces sospechosos."));
        b.add(new Escenario(6, "De: aulavirtual@udla.edu.ec\nAsunto: Nueva tarea publicada\nEl docente publicó la tarea 'Avance 2' en Programación II. Fecha de entrega: 30 de noviembre.",
                false, 1, "Notificación informativa del dominio oficial, sin pedir datos ni acciones urgentes."));
        b.add(new Escenario(7, "WhatsApp de Daniel (contacto guardado):\n¿Ya revisaste el avance del proyecto? Lo subí al repositorio del grupo.",
                false, 1, "Mensaje de un contacto conocido, coherente con el trabajo en curso y sin enlaces ni pedidos de datos."));
        b.add(new Escenario(8, "De: no-reply@accounts.google.com\nAsunto: Alerta de seguridad\nSe inició sesión en tu cuenta desde un nuevo dispositivo Windows. Si fuiste tú, no necesitas hacer nada.",
                false, 1, "Alerta informativa del dominio real de Google; no pide hacer clic ni entregar datos."));
        b.add(new Escenario(9, "De: servicios@bancoandino-seguridad.com\nAsunto: Actualización obligatoria\nEstimado cliente, por normativa debe actualizar sus datos antes del viernes en https://bancoandino-seguridad.com/actualizar",
                true, 2, "Dominio que agrega '-seguridad' al del banco, saludo genérico y pedido de actualizar datos."));
        b.add(new Escenario(10, "De: tesoreria@udla.edu.ec.pagos-online.com\nAsunto: Pago pendiente de matrícula\nPara evitar recargos, pague con tarjeta en el enlace adjunto.",
                true, 2, "El dominio real es pagos-online.com; 'udla.edu.ec' es solo un subdominio para engañar."));
        b.add(new Escenario(11, "De: Microsoft 365 <no-reply@micros0ft-support.com>\nAsunto: Su buzón está lleno (99 %)\nHaga clic para ampliar el almacenamiento gratis.",
                true, 2, "El dominio cambia la letra 'o' por el número '0' (micros0ft) y crea urgencia con el buzón lleno."));
        b.add(new Escenario(12, "De: entregas@enviaexpress-ec.net\nAsunto: Paquete retenido\nSu paquete no pudo ser entregado. Pague $1,50 de reenvío aquí: https://cutt.ly/paquete",
                true, 2, "Cobro pequeño para robar la tarjeta, acortador de enlaces y un paquete que no esperabas."));
        b.add(new Escenario(13, "De: becas@udla.edu.ec\nAsunto: Convocatoria de becas 2027\nLas postulaciones abren el 1 de diciembre. Revisa los requisitos entrando tú mismo a udla.edu.ec > Becas. No respondas con datos personales.",
                false, 2, "Aunque habla de becas, viene del dominio oficial y te pide entrar por tu cuenta al sitio oficial."));
        b.add(new Escenario(14, "De: alertas@bancoandino.com\nAsunto: Consumo con tu tarjeta\nSe registró un consumo de $25,40. Si no lo reconoces, llama al número que aparece detrás de tu tarjeta.",
                false, 2, "No contiene enlaces y te pide usar el número oficial de tu tarjeta, no uno del mensaje."));
        b.add(new Escenario(15, "De: coordinacion.ciberseguridad@udla.edu.ec\nAsunto: Cambio de aula\nLa clase de mañana se dicta en el laboratorio 3 del bloque 2.",
                false, 2, "Mensaje informativo del dominio oficial, sin enlaces ni pedidos de datos."));
        b.add(new Escenario(16, "De: noreply@github.com\nAsunto: [GitHub] Se añadió una nueva clave SSH\nSi no fuiste tú, revisa la seguridad de tu cuenta entrando directamente a github.com.",
                false, 2, "Dominio real de GitHub y te indica entrar directamente al sitio, sin enlaces de inicio de sesión."));
        b.add(new Escenario(17, "De: Rector UDLA <rector.udla@outlook.com>\nAsunto: Favor confidencial\nEstoy en una reunión y no puedo hablar. Compra 3 tarjetas de regalo de $100 y envíame los códigos. Te reembolso hoy.",
                true, 3, "Fraude del 'jefe': autoridad, secreto y urgencia desde un correo gratuito. La UDLA nunca pide tarjetas de regalo."));
        b.add(new Escenario(18, "De: soporte@udla.edu.ec (responder a: helpdesk.udla@protonmail.com)\nAsunto: Migración de la verificación en dos pasos\nResponde a este correo con el código de 6 dígitos que te llegará por SMS.",
                true, 3, "La respuesta va a otro dominio y pide el código de verificación, que nunca se comparte con nadie."));
        b.add(new Escenario(19, "De: notificaciones@docs-compartidos.com\nAsunto: Daniel Mora compartió 'Avance_Proyecto_Final.docx'\nAbre el documento en https://docs-compartidos.com/login (inicia sesión con tu correo institucional).",
                true, 3, "Usa el nombre de un compañero para generar confianza y lleva a una página falsa que pide tu correo y contraseña."));
        b.add(new Escenario(20, "De: seguridad@bancoandino.com\nAsunto: Acceso inusual detectado\nPara proteger su cuenta, instale el certificado de seguridad adjunto: Certificado_Seguridad.iso",
                true, 3, "El remitente parece real (puede estar falsificado), pero ningún banco envía archivos .iso para instalar."));
        b.add(new Escenario(21, "De: it-soporte@udla.edu.ec\nAsunto: Mantenimiento programado\nEl sábado de 22:00 a 02:00 el aula virtual no estará disponible. No es necesario realizar ninguna acción.",
                false, 3, "Comunicado informativo del dominio oficial que expresamente no pide ninguna acción."));
        b.add(new Escenario(22, "De: seguridad@bancoandino.com\nAsunto: Cambio de contraseña realizado\nTu contraseña de banca en línea se cambió hoy a las 10:32. Si no fuiste tú, llama a la línea oficial del banco.",
                false, 3, "Notificación de un cambio que tú hiciste; no tiene enlaces y remite a la línea oficial."));
        b.add(new Escenario(23, "De: Daniel Mora <dmora@udla.edu.ec>\nAsunto: Enlace del repositorio\nTe paso el repo para que revises la clase GestorPhishing: https://github.com/equipo-escudo/escudo-digital",
                false, 3, "Compañero conocido, correo institucional, enlace a github.com y coherente con el proyecto."));
        b.add(new Escenario(24, "De: account-security-noreply@accountprotection.microsoft.com\nAsunto: Código de verificación\nUsa el código 482913 para verificar tu identidad. Si no lo solicitaste, ignora este mensaje y no lo compartas.",
                false, 3, "Subdominio real de microsoft.com; entrega un código y advierte no compartirlo, en lugar de pedirlo."));
        return b;
    }
}
