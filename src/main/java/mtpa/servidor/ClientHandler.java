package mtpa.servidor;

import mtpa.excepciones.MtpaExcepcion;
import mtpa.gestores.GestorLogs;
import mtpa.gestores.GestorSalones;
import mtpa.gestores.GestorUsuarios;
import mtpa.modelo.Mensaje;
import mtpa.modelo.Usuario;
import mtpa.protocolo.MtpaPeticion;
import mtpa.protocolo.MtpaRespuestaError;
import mtpa.protocolo.MtpaRespuestaOk;
import mtpa.validadores.impl.Validador;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Gestiona la conexión de un cliente concreto.
 * Cada instancia atiende a un único cliente en su propio hilo.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ClientHandler extends Thread {

    private static final int MAX_USUARIOS_SALON = 15;

    private Socket socket;
    private GestorUsuarios gestorUsuarios;
    private GestorSalones gestorSalones;
    private ArrayList<ClientHandler> clientes; //lista compartida de todos los clientes conectados

    private Validador validador;

    private String username;    //null hasta que hace LOGIN
    private String salonActual; //salón en el que está conectado, null si ninguno
    private String privActual;  //usuario con quien tiene conversación privada activa
    private int fallosHeartbeat; //contador de heartbeats fallidos consecutivos

    public ClientHandler(Socket socket, GestorUsuarios gestorUsuarios, GestorSalones gestorSalones, ArrayList<ClientHandler> clientes) {
        this.socket = socket;
        this.gestorUsuarios = gestorUsuarios;
        this.gestorSalones = gestorSalones;
        this.clientes = clientes;
        this.validador = new Validador();
    }

    @Override
    public void run() {
        try {
            InputStream is = socket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int b;
            //Se leen bytes uno a uno hasta que llega \n (fin de trama) o se cierra la conexión
            while ((b = is.read()) != -1) {
                if (b == '\n') {
                    String mensaje = buffer.toString("UTF-8");
                    buffer.reset();
                    if (!mensaje.isEmpty()) {
                        procesarMensaje(mensaje);
                    }
                } else {
                    buffer.write(b);
                }
            }
        } catch (Exception e) {
            GestorLogs.error(username != null ? username : "DESCONOCIDO", "CLIENT_HANDLER", "Conexión interrumpida: " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    //Valida, parsea y despacha el mensaje recibido al comando correspondiente
    private void procesarMensaje(String msg) {
        try {
            //Primero se valida el formato del mensaje
            validador.validar(msg);
            MtpaPeticion peticion = MtpaPeticion.construir(msg);

            //Solo se permite REGISTER, LOGIN y HEARTBEAT sin autenticación previa
            if (username == null
                    && !peticion.getComando().equals(MtpaPeticion.REGISTER)
                    && !peticion.getComando().equals(MtpaPeticion.LOGIN)
                    && !peticion.getComando().equals(MtpaPeticion.HEARTBEAT)) {
                throw new MtpaExcepcion(MtpaExcepcion.USUARIO_NO_AUTENTICADO);
            }

            switch (peticion.getComando()) {

                case MtpaPeticion.REGISTER: {
                    String nuevoUsername = peticion.getParametro(0);
                    Usuario usuario = gestorUsuarios.registrar(nuevoUsername);
                    enviar(new MtpaRespuestaOk("REGISTER_OK|" + usuario.getKey()).toString());
                    break;
                }

                case MtpaPeticion.LOGIN: {
                    String loginUsername = peticion.getParametro(0);
                    int key;
                    try {
                        key = Integer.parseInt(peticion.getParametro(1));
                    } catch (NumberFormatException e) {
                        throw new MtpaExcepcion(MtpaExcepcion.FORMATO_INCORRECTO);
                    }
                    gestorUsuarios.login(loginUsername, key);
                    username = loginUsername;
                    //Tras el login se devuelven los salones disponibles
                    enviar(new MtpaRespuestaOk("LOGIN_OK|" + gestorSalones.listarSalones()).toString());
                    break;
                }

                case MtpaPeticion.LIST_ROOMS: {
                    enviar(new MtpaRespuestaOk("ROOMS_LIST|" + gestorSalones.listarSalones()).toString());
                    break;
                }

                case MtpaPeticion.JOIN_ROOM: {
                    String salon = peticion.getParametro(0);
                    gestorSalones.validarSalon(salon);
                    //Se comprueba que el salón no está lleno antes de entrar
                    if (contarUsuariosEnSalon(salon) >= MAX_USUARIOS_SALON) {
                        throw new MtpaExcepcion(MtpaExcepcion.SALON_LLENO);
                    }
                    salonActual = salon;
                    GestorLogs.info(username, "JOIN_ROOM", "Entró al salón " + salon);
                    //Confirmación de entrada al salón
                    enviar(new MtpaRespuestaOk("JOIN_OK|" + salon).toString());
                    //Lista de usuarios actualmente conectados al salón
                    enviar(new MtpaRespuestaOk("ROOM_USERS|" + salon + "|" + listarUsuariosEnSalon(salon)).toString());
                    //Carga automática de los mensajes del día actual
                    ArrayList<Mensaje> ultimas24h = gestorSalones.getHistorial(salon, LocalDate.now());
                    String historial = "HISTORY_DATA|" + salon + "|";
                    for (Mensaje m : ultimas24h) {
                        historial = historial + m.getTimestamp() + ";" + m.getUsername() + ";" + m.getContenido() + "#";
                    }
                    enviar(new MtpaRespuestaOk(historial).toString());
                    //Notificación al resto del salón
                    String notifEntrada = "NOTIF|" + salon + "|El usuario " + username + " ha entrado al salón";
                    for (ClientHandler cliente : clientes) {
                        if (salon.equals(cliente.salonActual) && !cliente.equals(this)) {
                            cliente.enviar(new MtpaRespuestaOk(notifEntrada).toString());
                        }
                    }
                    break;
                }

                case MtpaPeticion.LEAVE_ROOM: {
                    String salonSalido = peticion.getParametro(0);
                    if (!salonSalido.equals(salonActual)) {
                        throw new MtpaExcepcion(MtpaExcepcion.SALON_NO_VALIDO);
                    }
                    salonActual = null;
                    GestorLogs.info(username, "LEAVE_ROOM", "Salió del salón");
                    //Notificación al resto del salón de que el usuario ha salido
                    String notifSalida = "NOTIF|" + salonSalido + "|El usuario " + username + " ha salido del salón";
                    for (ClientHandler cliente : clientes) {
                        if (salonSalido.equals(cliente.salonActual) && !cliente.equals(this)) {
                            cliente.enviar(new MtpaRespuestaOk(notifSalida).toString());
                        }
                    }
                    break;
                }

                case MtpaPeticion.SEND_ROOM: {
                    //Si el servidor está en mantenimiento se rechaza el envío
                    if (Servidor.isMantenimiento()) {
                        throw new MtpaExcepcion(MtpaExcepcion.SERVIDOR_MANTENIMIENTO);
                    }
                    String salon = peticion.getParametro(0);
                    if (!salon.equals(salonActual)) {
                        throw new MtpaExcepcion(MtpaExcepcion.SALON_NO_VALIDO);
                    }
                    String contenido = peticion.getParametro(1);
                    Mensaje mensaje = gestorSalones.guardarMensaje(salon, username, contenido);
                    //Difusión del mensaje a todos los clientes del salón (incluido el remitente)
                    String trama = "PUSH_ROOM|" + salon + "|" + mensaje.getTimestamp() + "|" + mensaje.getUsername() + "|" + mensaje.getContenido();
                    for (ClientHandler cliente : clientes) {
                        if (salon.equals(cliente.salonActual)) {
                            cliente.enviar(new MtpaRespuestaOk(trama).toString());
                        }
                    }
                    break;
                }

                case MtpaPeticion.GET_HISTORY: {
                    String salon = peticion.getParametro(0);
                    LocalDate fecha;
                    try {
                        fecha = LocalDate.parse(peticion.getParametro(1));
                    } catch (Exception e) {
                        throw new MtpaExcepcion(MtpaExcepcion.FORMATO_INCORRECTO);
                    }
                    ArrayList<Mensaje> historial = gestorSalones.getHistorial(salon, fecha);
                    String resultado = "HISTORY_DATA|" + salon + "|";
                    for (Mensaje m : historial) {
                        resultado += m.getTimestamp() + ";" + m.getUsername() + ";" + m.getContenido() + "#";
                    }
                    enviar(new MtpaRespuestaOk(resultado).toString());
                    break;
                }

                case MtpaPeticion.JOIN_PRIV: {
                    String destino = peticion.getParametro(0);
                    if (!gestorUsuarios.estaConectado(destino)) {
                        throw new MtpaExcepcion(MtpaExcepcion.USUARIO_NO_CONECTADO);
                    }
                    privActual = destino;
                    GestorLogs.info(username, "JOIN_PRIV", "Conversación privada con " + destino);
                    enviar(new MtpaRespuestaOk("JOIN_PRIV_OK|" + destino).toString());
                    //Se avisa al usuario destino de la apertura del salón privado
                    for (ClientHandler cliente : clientes) {
                        if (destino.equals(cliente.username)) {
                            cliente.privActual = username;
                            cliente.enviar(new MtpaRespuestaOk("PRIV_INVITE|" + username).toString());
                            break;
                        }
                    }
                    break;
                }

                case MtpaPeticion.LEAVE_PRIV: {
                    String destinoCierre = peticion.getParametro(0);
                    if (!destinoCierre.equals(privActual)) {
                        throw new MtpaExcepcion(MtpaExcepcion.USUARIO_NO_CONECTADO);
                    }
                    privActual = null;
                    GestorLogs.info(username, "LEAVE_PRIV", "Salió de conversación privada");
                    //Se notifica al otro usuario que el privado se ha cerrado
                    for (ClientHandler cliente : clientes) {
                        if (destinoCierre.equals(cliente.username)) {
                            cliente.enviar(new MtpaRespuestaOk("PRIV_CLOSED|" + username).toString());
                            break;
                        }
                    }
                    break;
                }

                case MtpaPeticion.SEND_PRIV: {
                    //Si el servidor está en mantenimiento se rechaza el envío
                    if (Servidor.isMantenimiento()) {
                        throw new MtpaExcepcion(MtpaExcepcion.SERVIDOR_MANTENIMIENTO);
                    }
                    String destino = peticion.getParametro(0);
                    if (!destino.equals(privActual)) {
                        throw new MtpaExcepcion(MtpaExcepcion.USUARIO_NO_CONECTADO);
                    }
                    String contenido = peticion.getParametro(1);
                    //Se busca el ClientHandler del destinatario y se le reenvía el mensaje
                    String trama = "PUSH_PRIV|" + username + "|" + contenido;
                    for (ClientHandler cliente : clientes) {
                        if (destino.equals(cliente.username)) {
                            cliente.enviar(new MtpaRespuestaOk(trama).toString());
                            break;
                        }
                    }
                    GestorLogs.info(username, "SEND_PRIV", "Mensaje privado a " + destino);
                    break;
                }

                case MtpaPeticion.HEARTBEAT: {
                    //El cliente sigue vivo, se reinicia el contador
                    fallosHeartbeat = 0;
                    enviar(new MtpaRespuestaOk("HEARTBEAT_OK").toString());
                    break;
                }

                default: {
                    throw new MtpaExcepcion(MtpaExcepcion.COMANDO_DESCONOCIDO);
                }
            }

        } catch (MtpaExcepcion e) {
            //Se formatea el código de error al formato ERR_001, ERR_002, etc.
            String codigoFormateado = String.format("ERR_%03d", e.getCodigo());
            enviar(new MtpaRespuestaError(codigoFormateado, e.getMessage()).toString());
        } catch (Exception e) {
            GestorLogs.error(username != null ? username : "DESCONOCIDO", "PROCESAR_MENSAJE", "Error inesperado: " + e.getMessage());
        }
    }

    //Envía un mensaje al cliente de este handler
    public void enviar(String msg) {
        try {
            socket.getOutputStream().write(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            GestorLogs.error(username != null ? username : "DESCONOCIDO", "ENVIAR", "Error al enviar: " + e.getMessage());
        }
    }

    //Cuenta cuántos clientes están actualmente en un salón dado
    private int contarUsuariosEnSalon(String salon) {
        int contador = 0;
        for (ClientHandler cliente : clientes) {
            if (salon.equals(cliente.salonActual)) {
                contador++;
            }
        }
        return contador;
    }

    //Devuelve los usuarios actualmente conectados a un salón separados por coma
    private String listarUsuariosEnSalon(String salon) {
        String resultado = "";
        for (ClientHandler cliente : clientes) {
            if (salon.equals(cliente.salonActual) && cliente.username != null) {
                resultado = resultado + cliente.username + ",";
            }
        }
        //Se quita la última coma si la hay
        if (resultado.endsWith(",")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }

    //Cierra la conexión, hace logout y elimina al cliente de la lista compartida
    private void desconectar() {
        if (salonActual != null) {
            String notifSalida = "NOTIF|" + salonActual + "|El usuario " + username + " ha salido del salón";
            for (ClientHandler cliente : clientes) {
                if (salonActual.equals(cliente.salonActual) && !cliente.equals(this)) {
                    cliente.enviar(new MtpaRespuestaOk(notifSalida).toString());
                }
            }
        }
        if (privActual != null) {
            for (ClientHandler cliente : clientes) {
                if (privActual.equals(cliente.username)) {
                    cliente.enviar(new MtpaRespuestaOk("PRIV_CLOSED|" + username).toString());
                    break;
                }
            }
        }

        if (username != null) {
            gestorUsuarios.logout(username);
        }
        clientes.remove(this);
        try {
            socket.close();
        } catch (Exception e) {
            GestorLogs.error(username != null ? username : "DESCONOCIDO", "DESCONECTAR", "Error al cerrar socket: " + e.getMessage());
        }
        GestorLogs.info(username != null ? username : "DESCONOCIDO", "DESCONECTAR", "Cliente desconectado");
    }

    public String getUsername() {
        return username;
    }

    public String getSalonActual() {
        return salonActual;
    }

    public int getFallosHeartbeat() {
        return fallosHeartbeat;
    }

    public void incrementarFallosHeartbeat() {
        fallosHeartbeat++;
    }

    //Fuerza el cierre del socket, usado por el GestorHeartbeat cuando el cliente no responde
    public void forzarDesconexion() {
        try {
            socket.close();
        } catch (Exception e) {
            GestorLogs.error(username != null ? username : "DESCONOCIDO", "FORZAR_DESCONEXION", "Error: " + e.getMessage());
        }
    }
}
