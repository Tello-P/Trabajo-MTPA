package mtpa.modelo;

/**
 * Representa un usuario registrado en el sistema.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class Usuario {

    private String username;
    private int key;
    private boolean online;

    public Usuario(String username, int key) {
        this.username = username;
        this.key = key;
        this.online = false;
    }

    public String getUsername() {
        return username;
    }

    public int getKey() {
        return key;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
