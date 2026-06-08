package mtpa.cliente;

import java.io.FileWriter;
import java.time.LocalDateTime;

/**
 * Gestiona los logs del cliente escribiendo en fichero y en consola.
 * Formato: [TIMESTAMP] | [NIVEL: INFO/ERROR] | [USER] | [OPERACIÓN] | [DETALLE]
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class LogsCliente {

    private static final String FICHERO_LOGS = "cliente.log";

    public static void info(String usuario, String operacion, String detalle) {
        escribir("INFO", usuario, operacion, detalle);
    }

    public static void error(String usuario, String operacion, String detalle) {
        escribir("ERROR", usuario, operacion, detalle);
    }

    private static void escribir(String nivel, String usuario, String operacion, String detalle) {
        String linea = "[" + LocalDateTime.now() + "] | [" + nivel + "] | [" + usuario + "] | [" + operacion + "] | [" + detalle + "]";
        System.out.println(linea);
        try (FileWriter fw = new FileWriter(FICHERO_LOGS, true)) {
            fw.write(linea + "\n");
        } catch (Exception e) {
            System.out.println("Error al escribir en el fichero de logs del cliente: " + e.getMessage());
        }
    }
}
