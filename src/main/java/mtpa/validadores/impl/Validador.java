package mtpa.validadores.impl;

import mtpa.excepciones.MtpaExcepcion;
import mtpa.protocolo.MtpaPeticion;
import mtpa.protocolo.MtpaProtocolo;
import mtpa.validadores.IValidador;

/**
 * Implementación del validador del protocolo MTPA.
 * Comprueba que el mensaje recibido tiene el formato correcto antes de procesarlo.
 * En caso de no tener el formato correcto, se lanza la excepción correspondiente.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class Validador implements IValidador {

    private static final int MAX_CARACTERES = 190;

    @Override
    public void validar(String msg) throws MtpaExcepcion {
        if (msg == null || msg.trim().isEmpty()) {
            throw new MtpaExcepcion(MtpaExcepcion.FORMATO_INCORRECTO);
        }

        String[] fragmentos = msg.trim().split(MtpaProtocolo.DELIMITADOR);
        String comando = fragmentos[0];
        int parametrosRecibidos = fragmentos.length - 1;

        if (parametrosRecibidos != parametrosEsperados(comando)) {
            throw new MtpaExcepcion(MtpaExcepcion.FORMATO_INCORRECTO);
        }

        if (comando.equals(MtpaPeticion.SEND_ROOM) || comando.equals(MtpaPeticion.SEND_PRIV)) {
            String contenido = fragmentos[fragmentos.length - 1];
            if (contenido.length() > MAX_CARACTERES) {
                throw new MtpaExcepcion(MtpaExcepcion.MENSAJE_MUY_LARGO);
            }
        }
    }

    private int parametrosEsperados(String comando) throws MtpaExcepcion {
        switch (comando) {
            case MtpaPeticion.REGISTER:    return 1;
            case MtpaPeticion.LOGIN:       return 2;
            case MtpaPeticion.LIST_ROOMS:  return 0;
            case MtpaPeticion.JOIN_ROOM:   return 1;
            case MtpaPeticion.LEAVE_ROOM:  return 1;
            case MtpaPeticion.SEND_ROOM:   return 2;
            case MtpaPeticion.GET_HISTORY: return 2;
            case MtpaPeticion.JOIN_PRIV:   return 1;
            case MtpaPeticion.LEAVE_PRIV:  return 1;
            case MtpaPeticion.SEND_PRIV:   return 2;
            case MtpaPeticion.HEARTBEAT:   return 1;
            default: throw new MtpaExcepcion(MtpaExcepcion.COMANDO_DESCONOCIDO);
        }
    }
}
