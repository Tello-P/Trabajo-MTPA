package mtpa.gestores;

import mtpa.excepciones.MtpaExcepcion;
import mtpa.modelo.Usuario;
import mtpa.protocolo.MtpaProtocolo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Gestiona los usuarios del sistema: registro, login, logout y consultas.
 * Mantiene la lista de usuarios en memoria y gestiona su persistencia en disco.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorUsuarios {

    private static final String FICHERO_USUARIOS = "usuarios.txt";

    private ArrayList<Usuario> usuarios;
    private int ultimaKey;

    public GestorUsuarios() {
        //Se usa al iniciar el servidor, cargar todos los usuarios en un array
        this.usuarios = cargarUsuarios();
        this.ultimaKey = calcularUltimaKey();
    }

    //Registra un nuevo usuario, genera key autonumérica y lo guarda en fichero
    public Usuario registrar(String username) throws MtpaExcepcion {
        if (existeUsuario(username)) {
            throw new MtpaExcepcion(MtpaExcepcion.USUARIO_YA_EXISTE);
        }
        ultimaKey++;
        Usuario nuevoUsuario = new Usuario(username, ultimaKey);
        usuarios.add(nuevoUsuario);
        guardarUsuario(nuevoUsuario);
        GestorLogs.info(username, "REGISTER", "Usuario registrado con key: " + ultimaKey);
        return nuevoUsuario;
    }

    //Valida credenciales y marca el usuario como online
    public Usuario login(String username, int key) throws MtpaExcepcion {
        Usuario usuario = buscarUsuario(username);
        if (usuario == null || usuario.getKey() != key) {
            throw new MtpaExcepcion(MtpaExcepcion.CREDENCIALES_INVALIDAS);
        }
        if (usuario.isOnline()) {
            throw new MtpaExcepcion(MtpaExcepcion.USUARIO_YA_CONECTADO);
        }
        usuario.setOnline(true);
        GestorLogs.info(username, "LOGIN", "Usuario conectado");
        return usuario;
    }

    //Marca el usuario como offline cuando se desconecta
    public void logout(String username) {
        Usuario usuario = buscarUsuario(username);
        if (usuario != null) {
            usuario.setOnline(false);
            GestorLogs.info(username, "LOGOUT", "Usuario desconectado");
        }
    }

    //Comprueba si un usuario está conectado en este momento
    public boolean estaConectado(String username) {
        Usuario usuario = buscarUsuario(username);
        if (usuario == null) {
            return false;
        }
        return usuario.isOnline();
    }

    public Usuario buscarUsuario(String username) {
        for (Usuario usuario : usuarios) {
            if (usuario.getUsername().equals(username)) {
                return usuario;
            }
        }
        return null;
    }

    public ArrayList<Usuario> getUsuariosConectados() {
        ArrayList<Usuario> conectados = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            if (usuario.isOnline()) {
                conectados.add(usuario);
            }
        }
        return conectados;
    }

    //Guarda linea al final del archivo con el usuario y su key
    private void guardarUsuario(Usuario usuario) {
        try (FileWriter fw = new FileWriter(FICHERO_USUARIOS, true)) {
            fw.write(usuario.getUsername() + MtpaProtocolo.DELIMITADOR_ENVIO + usuario.getKey() + "\n");
        } catch (Exception e) {
            GestorLogs.error("SISTEMA", "GUARDAR_USUARIO", "Error al guardar usuario en fichero: " + e.getMessage());
        }
    }

    //Se usa al iniciar el servidor, cargar todos los usuarios en un array
    //Trabajar con una lista en memoria es mas facil y rapido
    private ArrayList<Usuario> cargarUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FICHERO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                //Se parte la linea por | y se crea el objeto Usuario
                String[] fragmentos = linea.split(MtpaProtocolo.DELIMITADOR);
                String username = fragmentos[0];
                int key = Integer.parseInt(fragmentos[1]);
                lista.add(new Usuario(username, key));
            }
        } catch (Exception e) {
            GestorLogs.info("SISTEMA", "CARGAR_USUARIOS", "No se encontró fichero de usuarios, se creará uno nuevo");
        }
        return lista;
    }

    //Comprueba si un usuario ya está registrado en el sistema
    private boolean existeUsuario(String username) {
        Usuario usuario = buscarUsuario(username);
        if (usuario == null) {
            return false;
        }
        return true;
    }

    private int calcularUltimaKey() {
        int max = 0;
        for (Usuario usuario : usuarios) {
            if (usuario.getKey() > max) {
                max = usuario.getKey();
            }
        }
        return max;
    }
}
