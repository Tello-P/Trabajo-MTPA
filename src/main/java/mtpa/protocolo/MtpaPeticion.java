package mtpa.protocolo;

/**
 * Representa una petición del cliente al servidor ya parseada.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class MtpaPeticion extends MtpaProtocolo {

    public static final String REGISTER     = "REGISTER";
    public static final String LOGIN        = "LOGIN";
    public static final String LIST_ROOMS   = "LIST_ROOMS";
    public static final String JOIN_ROOM    = "JOIN_ROOM";
    public static final String LEAVE_ROOM   = "LEAVE_ROOM";
    public static final String SEND_ROOM    = "SEND_ROOM";
    public static final String GET_HISTORY  = "GET_HISTORY";
    public static final String JOIN_PRIV    = "JOIN_PRIV";
    public static final String LEAVE_PRIV   = "LEAVE_PRIV";
    public static final String SEND_PRIV    = "SEND_PRIV";
    public static final String HEARTBEAT    = "HEARTBEAT";

    private String comando;
    private String[] parametros;

    private MtpaPeticion() {
    }

    public static MtpaPeticion construir(String msg) {
        String[] fragmentos = msg.trim().split(DELIMITADOR);
        MtpaPeticion peticion = new MtpaPeticion();
        peticion.comando = fragmentos[0];
        peticion.parametros = new String[fragmentos.length - 1];
        for (int i = 1; i < fragmentos.length; i++) {
            peticion.parametros[i - 1] = fragmentos[i];
        }
        return peticion;
    }

    public String getComando() {
        return comando;
    }

    public String[] getParametros() {
        return parametros;
    }

    public String getParametro(int indice) {
        return parametros[indice];
    }

    public int totalParametros() {
        return parametros.length;
    }
}
