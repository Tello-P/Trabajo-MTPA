package mtpa.validadores;

import mtpa.excepciones.MtpaExcepcion;

/**
 * Interfaz para la validación de mensajes del protocolo MTPA.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public interface IValidador {

    void validar(String msg) throws MtpaExcepcion;
}
