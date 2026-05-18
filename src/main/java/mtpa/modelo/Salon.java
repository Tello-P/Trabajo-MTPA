package mtpa.modelo;

import java.util.ArrayList;

/**
 * Representa un salón de chat del sistema.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class Salon {

    private String nombre;
    private ArrayList<Mensaje> mensajes;
    private ArrayList<String> usuariosConectados;

    public Salon(String nombre) {
        this.nombre = nombre;
        this.mensajes = new ArrayList<>();
        this.usuariosConectados = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<Mensaje> getMensajes() {
        return mensajes;
    }

    public ArrayList<String> getUsuariosConectados() {
        return usuariosConectados;
    }
}
