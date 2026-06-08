package mtpa.protocolo;

/**
 * Clase base del protocolo de comunicación MTPA, define las tres constantes compartidas por las clases
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public abstract class MtpaProtocolo {
    

    public static final String DELIMITADOR = "\\|"; //Para construir mensajes
    public static final String DELIMITADOR_ENVIO = "|"; //Para ller lo que llega del cliente
    public static final String FIN_TRAMA = "\n"; //para saber cuando mesnaje termina
}
