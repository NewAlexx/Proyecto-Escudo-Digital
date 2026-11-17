package modelo;

public enum TipoDispositivo {
    LAPTOP("Laptop", true, Guias.COMPUTADORA),
    PC_ESCRITORIO("PC de escritorio", true, Guias.COMPUTADORA),
    SMARTPHONE("Smartphone", false, Guias.MOVIL),
    TABLET("Tablet", false, Guias.MOVIL);

    private final String nombre;
    private final boolean computadora;
    private final String guia;

    TipoDispositivo(String nombre, boolean computadora, String guia) {
        this.nombre = nombre;
        this.computadora = computadora;
        this.guia = guia;
    }

    public boolean esComputadora() { return computadora; }

    public String getGuiaInformacion() { return guia; }

    public String getPreguntaProteccion() {
        return computadora
                ? "¿Tiene un antivirus activo? (en Windows, Microsoft Defender viene incluido)"
                : "¿Tiene activa la protección del sistema? (Android: Google Play Protect; iPhone o iPad: actualizado, "
                  + "apps solo de la App Store y sin jailbreak)";
    }

    public String getRecomendacionProteccion() {
        return computadora
                ? "Activa un antivirus. En Windows, Microsoft Defender viene incluido y es gratuito: "
                  + "Inicio > Seguridad de Windows > Protección antivirus y contra amenazas."
                : "Los celulares y tablets no necesitan un antivirus instalado, pero sí su protección del sistema. "
                  + "En Android, abre Play Store > tu foto de perfil > Play Protect y actívalo. En iPhone o iPad, "
                  + "mantén el sistema actualizado, instala apps solo desde la App Store y no le hagas jailbreak.";
    }

    public String getRecomendacionCompartido() {
        return computadora
                ? "Crea una cuenta de usuario separada para cada persona que use el dispositivo, cada una con su propia contraseña "
                  + "(Windows: Configuración > Cuentas > Otros usuarios)."
                : "Los celulares y la mayoría de tablets no permiten varias cuentas de usuario. No compartas tu PIN o patrón; "
                  + "si prestas el dispositivo, fija una sola app (Android: Ajustes > Seguridad > Fijación de apps; "
                  + "iPhone o iPad: Ajustes > Accesibilidad > Acceso guiado) o usa el modo invitado si tu Android lo tiene, "
                  + "y protege con huella o PIN las apps sensibles como la banca, el correo y WhatsApp.";
    }

    @Override
    public String toString() { return nombre; }

    private static final class Guias {
        static final String COMPUTADORA = "Windows: Inicio > Configuración > Sistema > Información (versión) y "
                + "Windows Update > Historial de actualizaciones (fecha). macOS: menú Apple > Acerca de esta Mac "
                + "y Configuración del Sistema > General > Actualización de software.";
        static final String MOVIL = "Android: Ajustes > Acerca del teléfono > Versión de Android y "
                + "Ajustes > Sistema > Actualización de software (fecha). iPhone o iPad: Ajustes > General > "
                + "Información y Ajustes > General > Actualización de software.";
    }
}
