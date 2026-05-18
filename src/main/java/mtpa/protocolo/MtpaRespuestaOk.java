package mtpa.protocolo;

/**
 * Representa una respuesta de éxito del servidor al cliente.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class MtpaRespuestaOk extends MtpaProtocolo {

    private String respuesta;

    public MtpaRespuestaOk(String respuesta) {
        this.respuesta = respuesta;
    }

    @Override
    public String toString() {
        return respuesta + FIN_TRAMA;
    }
}
