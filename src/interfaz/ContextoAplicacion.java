package interfaz;

import negocio.Configuracion;
import negocio.GestorCredenciales;
import negocio.GestorDiagnostico;
import negocio.GestorDispositivos;
import negocio.GestorEntrenamiento;
import negocio.GestorPhishing;
import negocio.GestorRiesgoGlobal;
import negocio.GestorUsuarios;
import negocio.LeakCheckCliente;
import negocio.Persistencia;
import negocio.PwnedPasswordsCliente;
import negocio.VirusTotalCliente;
import negocio.XposedOrNotCliente;

public class ContextoAplicacion {
    private final GestorUsuarios gestorUsuarios;
    private final GestorDiagnostico gestorDiagnostico;
    private final GestorCredenciales gestorCredenciales;
    private final GestorDispositivos gestorDispositivos;
    private final GestorPhishing gestorPhishing;
    private final GestorEntrenamiento gestorEntrenamiento;
    private final GestorRiesgoGlobal gestorRiesgoGlobal;

    public ContextoAplicacion() {
        this.gestorUsuarios = Persistencia.cargar();
        this.gestorDiagnostico = new GestorDiagnostico();
        this.gestorCredenciales = new GestorCredenciales(new PwnedPasswordsCliente(), new XposedOrNotCliente(), new LeakCheckCliente());
        this.gestorDispositivos = new GestorDispositivos();
        String claveVT = Configuracion.obtener("vt.apikey");
        this.gestorPhishing = new GestorPhishing(claveVT == null ? null : new VirusTotalCliente(claveVT));
        this.gestorEntrenamiento = new GestorEntrenamiento();
        this.gestorRiesgoGlobal = new GestorRiesgoGlobal(gestorEntrenamiento);
    }

    public void guardar() {
        Persistencia.guardar(gestorUsuarios);
    }

    public GestorUsuarios getGestorUsuarios() { return gestorUsuarios; }
    public GestorDiagnostico getGestorDiagnostico() { return gestorDiagnostico; }
    public GestorCredenciales getGestorCredenciales() { return gestorCredenciales; }
    public GestorDispositivos getGestorDispositivos() { return gestorDispositivos; }
    public GestorPhishing getGestorPhishing() { return gestorPhishing; }
    public GestorEntrenamiento getGestorEntrenamiento() { return gestorEntrenamiento; }
    public GestorRiesgoGlobal getGestorRiesgoGlobal() { return gestorRiesgoGlobal; }
}
