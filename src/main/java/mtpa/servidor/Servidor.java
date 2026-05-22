package mtpa.servidor;

import mtpa.gestores.GestorHeartbeat;
import mtpa.gestores.GestorLogs;
import mtpa.gestores.GestorSalones;
import mtpa.gestores.GestorUsuarios;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Servidor principal del sistema MTPA.
 * Acepta conexiones de clientes y lanza un ClientHandler por cada uno de ellos.
 * Incluye una consola de administración que permite gestionar el servidor.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class Servidor implements Runnable {

    private static final int PUERTO = 8080;

    //Flags de estado del servidor controlados desde la consola de administración
    private static boolean bloqueado = false;
    private static boolean mantenimiento = false;

    private GestorUsuarios gestorUsuarios;
    private GestorSalones gestorSalones;
    private ArrayList<ClientHandler> clientes;

    public Servidor() {
        this.gestorUsuarios = new GestorUsuarios();
        this.gestorSalones = new GestorSalones();
        this.clientes = new ArrayList<>();
    }

    //Hilo de escucha conexiones
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            GestorLogs.info("SISTEMA", "INICIO", "Servidor escuchando en puerto " + PUERTO);

            //Se arranca el hilo de heartbeat para controlar los clientes inactivos
            GestorHeartbeat heartbeat = new GestorHeartbeat(clientes);
            heartbeat.start();

            //Bucle infinito de aceptación de conexiones
            while (true) {
                Socket socket = serverSocket.accept();
                //Si el servidor está bloqueado se rechaza la conexión enviando ERR_005
                if (bloqueado) {
                    String ip = socket.getInetAddress().toString();
                    socket.getOutputStream().write("ERROR|ERR_005|Servidor en mantenimiento\n".getBytes("UTF-8"));
                    socket.close();
                    GestorLogs.info("SISTEMA", "CONEXION_RECHAZADA", "Conexión rechazada por bloqueo desde " + ip);
                    continue;
                }
                ClientHandler cliente = new ClientHandler(socket, gestorUsuarios, gestorSalones, clientes);
                clientes.add(cliente);
                cliente.start();
                GestorLogs.info("SISTEMA", "NUEVA_CONEXION", "Cliente conectado desde " + socket.getInetAddress());
            }
        } catch (Exception e) {
            GestorLogs.error("SISTEMA", "SERVIDOR", "Error en el servidor: " + e.getMessage());
        }
    }

    //Consola de administración: lee comandos por teclado y ejecuta acciones
    private void consolaAdmin() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Consola de administración activa. Escribe 'ayuda' para ver los comandos.");
            String comando;
            while ((comando = br.readLine()) != null) {
                switch (comando.trim()) {
                    case "lista":
                        listarConectados();
                        break;
                    case "salones":
                        mostrarUsuariosPorSalon();
                        break;
                    case "mensajes":
                        mostrarMensajesPorSalon();
                        break;
                    case "bloquear":
                        bloqueado = !bloqueado;
                        System.out.println("Servidor " + (bloqueado ? "BLOQUEADO" : "DESBLOQUEADO") + " (aceptación de nuevos clientes)");
                        GestorLogs.info("SISTEMA", "ADMIN", "Bloqueo " + (bloqueado ? "activado" : "desactivado"));
                        break;
                    case "mantenimiento":
                        mantenimiento = !mantenimiento;
                        System.out.println("Modo mantenimiento " + (mantenimiento ? "ACTIVO" : "INACTIVO") + " (envío de mensajes)");
                        GestorLogs.info("SISTEMA", "ADMIN", "Mantenimiento " + (mantenimiento ? "activado" : "desactivado"));
                        break;
                    case "parar":
                        GestorLogs.info("SISTEMA", "ADMIN", "Servidor detenido por el administrador");
                        System.exit(0);
                        break;
                    case "ayuda":
                        System.out.println("Comandos disponibles:");
                        System.out.println(" lista          (para ver usuarios conectados)");
                        System.out.println(" salones        (usuarios por salón)");
                        System.out.println(" mensajes       (mensajes por salón)");
                        System.out.println(" bloquear       (no aceptar/aceptar nuevos clientes)");
                        System.out.println(" mantenimiento  (parar/activar mensajería)");
                        System.out.println(" parar          (detiene el servidor)");
                        System.out.println(" ayuda          (muestra opciones de ayuda)");
                        break;
                    default:
                        System.out.println("Comando desconocido. Escribe 'ayuda' para ver los comandos.");
                }
            }
        } catch (Exception e) {
            GestorLogs.error("SISTEMA", "ADMIN", "Error en consola de administración: " + e.getMessage());
        }
    }

    //Muestra por consola los usuarios actualmente conectados
    private void listarConectados() {
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes conectados.");
            return;
        }
        System.out.println("Clientes conectados:");
        for (ClientHandler cliente : clientes) {
            String nombre = cliente.getUsername() != null ? cliente.getUsername() : "(sin login)";
            String salon = cliente.getSalonActual() != null ? cliente.getSalonActual() : "ninguno";
            System.out.println("  - " + nombre + " (salón: " + salon + ")");
        }
    }

    //Muestra el número de usuarios actualmente conectados a cada salón
    private void mostrarUsuariosPorSalon() {
        String[] nombres = gestorSalones.listarSalones().split(",");
        System.out.println("Usuarios por salón:");
        for (String salon : nombres) {
            int contador = 0;
            for (ClientHandler cliente : clientes) {
                if (salon.equals(cliente.getSalonActual())) {
                    contador++;
                }
            }
            System.out.println("  " + salon + ": " + contador + " usuarios");
        }
    }

    //Muestra el número total de mensajes guardados en cada salón (leyendo del fichero)
    private void mostrarMensajesPorSalon() {
        String[] nombres = gestorSalones.listarSalones().split(",");
        System.out.println("Mensajes por salón:");
        for (String salon : nombres) {
            int contador = 0;
            try (BufferedReader br = new BufferedReader(new FileReader("salones/" + salon + ".txt"))) {
                while (br.readLine() != null) {
                    contador++;
                }
            } catch (Exception e) {
                //Si no existe el fichero, se queda en 0 mensajes
            }
            System.out.println("  " + salon + ": " + contador + " mensajes");
        }
    }

    public static boolean isBloqueado() {
        return bloqueado;
    }

    public static boolean isMantenimiento() {
        return mantenimiento;
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        //El bucle de aceptación de conexiones corre en su propio hilo
        Thread hiloServidor = new Thread(servidor);
        hiloServidor.start();
        //El hilo principal queda dedicado a la consola de administración
        servidor.consolaAdmin();
    }
}
