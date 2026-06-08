package mtpa.modelo;

import java.time.LocalDateTime;

/**
 * Representa un mensaje enviado en un salón.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class Mensaje {

    private LocalDateTime timestamp;
    private String username;
    private String contenido;

    public Mensaje(String username, String contenido) {
        this.timestamp = LocalDateTime.now();
        this.username = username;
        this.contenido = contenido;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getContenido() {
        return contenido;
    }
}
