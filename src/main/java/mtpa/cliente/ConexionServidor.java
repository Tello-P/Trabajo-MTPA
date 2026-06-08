package mtpa.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Gestiona la conexión TCP con el servidor MTPA.
 * Abre el socket, envía tramas UTF-8 terminadas en \n y arranca
 * los hilos de lectura de respuestas y de heartbeat periódico.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ConexionServidor {

    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private Thread hiloLector;
    private Thread hiloHeartbeat;

    public ConexionServidor(String host, int puerto) throws IOException {
        socket = new Socket(host, puerto);
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), false);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        LogsCliente.info("CLIENTE", "CONECTAR", "Conexión establecida con " + host + ":" + puerto);
    }

    //Envía una trama al servidor añadiendo \n al final. Sincronizado para uso desde varios hilos
    public synchronized void enviar(String msg) {
        writer.print(msg + "\n");
        writer.flush();
    }

    //Arranca el hilo lector pasando cada línea recibida al callback
    //Si el servidor cierra la conexión o hay error, se llama al callback con "__DESCONECTADO__"
    //Solo se puede llamar una vez; llamadas posteriores no hacen nada
    public void iniciarLector(Consumer<String> callback) {
        if (hiloLector != null) return;
        hiloLector = new Thread(() -> {
            try {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    callback.accept(linea);
                }
            } catch (IOException ignored) {
                //socket cerrado, se notifica abajo
            }
            LogsCliente.error("CLIENTE", "LECTOR", "Conexión perdida con el servidor");
            callback.accept("__DESCONECTADO__");
        });
        hiloLector.setDaemon(true);
        hiloLector.start();
    }

    //Arranca el hilo de heartbeat que envía HEARTBEAT|0 cada 20 segundos
    //Evita que el servidor corte la conexión por inactividad
    //Solo se puede llamar una vez; llamadas posteriores no hacen nada
    public void iniciarHeartbeat() {
        if (hiloHeartbeat != null) return;
        hiloHeartbeat = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(20_000);
                    enviar("HEARTBEAT|0");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        hiloHeartbeat.setDaemon(true);
        hiloHeartbeat.start();
    }

    //Cierra la conexión. Se cierra el socket primero para desbloquear el readLine del hilo lector
    public void cerrar() {
        LogsCliente.info("CLIENTE", "CERRAR", "Cerrando conexión con el servidor");
        try { socket.close(); } catch (IOException ignored) {}
        if (hiloLector    != null) hiloLector.interrupt();
        if (hiloHeartbeat != null) hiloHeartbeat.interrupt();
    }
}
