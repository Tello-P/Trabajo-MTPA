package mtpa.protocolo;

/**
 * Representa una respuesta de error del servidor al cliente.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class MtpaRespuestaError extends MtpaProtocolo {

    public static final String ERR_001 = "ERR_001";
    public static final String ERR_002 = "ERR_002";
    public static final String ERR_003 = "ERR_003";
    public static final String ERR_004 = "ERR_004";
    public static final String ERR_005 = "ERR_005";
    public static final String ERR_006 = "ERR_006";
    public static final String ERR_007 = "ERR_007";
    public static final String ERR_008 = "ERR_008";
    public static final String ERR_009 = "ERR_009";
    public static final String ERR_010 = "ERR_010";
    public static final String ERR_011 = "ERR_011";
    public static final String ERR_012 = "ERR_012";

    private String codigoError;
    private String descripcion;

    public MtpaRespuestaError(String codigoError, String descripcion) {
        this.codigoError = codigoError;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "ERROR" + DELIMITADOR_ENVIO + codigoError + DELIMITADOR_ENVIO + descripcion + FIN_TRAMA;
    }
}
