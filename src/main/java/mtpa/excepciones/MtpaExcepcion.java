package mtpa.excepciones;

/**
 * Excepciones capturadas por el codigo de errores
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class MtpaExcepcion extends Exception {

    public static final int USUARIO_YA_EXISTE       = 1;
    public static final int MENSAJE_MUY_LARGO       = 2;
    public static final int CREDENCIALES_INVALIDAS  = 3;
    public static final int SALON_NO_VALIDO         = 4;
    public static final int SERVIDOR_MANTENIMIENTO  = 5;
    public static final int USUARIO_NO_CONECTADO    = 6;
    public static final int TIMEOUT_HEARTBEAT       = 7;
    public static final int SALON_LLENO             = 8;
    public static final int COMANDO_DESCONOCIDO     = 9;
    public static final int USUARIO_YA_CONECTADO    = 10;
    public static final int FORMATO_INCORRECTO      = 11;
    public static final int USUARIO_NO_AUTENTICADO  = 12;

    private int codigo;
    
    //Se llama a la clase exception
    public MtpaExcepcion(int codigo) {
        super(obtenerMensaje(codigo)); //Guarda el texto de la exception
        this.codigo = codigo; //Guarda el código de error
    }

    //Se genra el mensaje de error correspondiente a cada codigo
    private static String obtenerMensaje(int codigo) {
        switch (codigo) {
            case USUARIO_YA_EXISTE:      return "El nombre de usuario ya existe";
            case MENSAJE_MUY_LARGO:      return "El mensaje excede los 190 caracteres";
            case CREDENCIALES_INVALIDAS: return "Credenciales inválidas";
            case SALON_NO_VALIDO:        return "Salón no válido";
            case SERVIDOR_MANTENIMIENTO: return "Servidor en mantenimiento";
            case USUARIO_NO_CONECTADO:   return "Usuario destino no conectado";
            case TIMEOUT_HEARTBEAT:      return "Conexión perdida por timeout";
            case SALON_LLENO:            return "Salón lleno";
            case COMANDO_DESCONOCIDO:    return "Comando no reconocido";
            case USUARIO_YA_CONECTADO:   return "Usuario ya conectado";
            case FORMATO_INCORRECTO:     return "Formato incorrecto";
            case USUARIO_NO_AUTENTICADO: return "Usuario no autenticado";
            default:                     return "Error desconocido";
        }
    }

    public int getCodigo() {
        return codigo;
    }
}
