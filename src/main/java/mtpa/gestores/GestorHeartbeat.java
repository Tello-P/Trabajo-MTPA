package mtpa.gestores;

import mtpa.servidor.ClientHandler;
import java.util.ArrayList;

/**
 * Gestor encargado de comprobar que los clientes siguen vivos.
 * Cada cierto tiempo incrementa el contador de fallos de cada cliente.
 * Si un cliente envía HEARTBEAT, el contador se reinicia.
 * Si el contador llega al máximo, se le desconecta.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorHeartbeat extends Thread {

    private static final int INTERVALO_MS = 30000; //cada cuánto se comprueba (30 segundos)
    private static final int MAX_FALLOS = 2;       //cuántos fallos consecutivos antes de desconectar

    private ArrayList<ClientHandler> clientes;

    public GestorHeartbeat(ArrayList<ClientHandler> clientes) {
        this.clientes = clientes;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(INTERVALO_MS);
                //Se recorre la lista al revés por si hay que eliminar elementos
                for (int i = clientes.size() - 1; i >= 0; i--) {
                    ClientHandler cliente = clientes.get(i);
                    cliente.incrementarFallosHeartbeat();
                    if (cliente.getFallosHeartbeat() >= MAX_FALLOS) {
                        //Se fuerza el cierre, el run() del ClientHandler hará el resto (logout y limpieza)
                        String nombre = cliente.getUsername() != null ? cliente.getUsername() : "DESCONOCIDO";
                        GestorLogs.info(nombre, "HEARTBEAT", "Cliente sin respuesta, desconectando");
                        cliente.forzarDesconexion();
                    }
                }
            } catch (Exception e) {
                GestorLogs.error("SISTEMA", "HEARTBEAT", "Error en hilo de heartbeat: " + e.getMessage());
            }
        }
    }
}
