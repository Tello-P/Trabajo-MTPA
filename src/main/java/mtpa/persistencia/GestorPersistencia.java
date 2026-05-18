package mtpa.persistencia;

import mtpa.modelo.Mensaje;
import mtpa.modelo.Usuario;
import mtpa.protocolo.MtpaProtocolo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la lectura y escritura de datos en ficheros para mantener
 * la información persistente entre reinicios del servidor.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorPersistencia {

    private static final Logger logger = Logger.getLogger(GestorPersistencia.class.getName());

    private static final String FICHERO_USUARIOS   = "usuarios.txt";
    private static final String DIRECTORIO_SALONES = "salones/";

    public GestorPersistencia() {
        new java.io.File(DIRECTORIO_SALONES).mkdirs();
    }

    public void guardarUsuario(Usuario usuario) {
        try (FileWriter fw = new FileWriter(FICHERO_USUARIOS, true)) {
            fw.write(usuario.getUsername() + MtpaProtocolo.DELIMITADOR_ENVIO + usuario.getKey() + "\n");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar usuario", e);
        }
    }

    public ArrayList<Usuario> cargarUsuarios() {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FICHERO_USUARIOS))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] fragmentos = linea.split(MtpaProtocolo.DELIMITADOR);
                if (fragmentos.length == 2) {
                    String username = fragmentos[0];
                    int key = Integer.parseInt(fragmentos[1]);
                    usuarios.add(new Usuario(username, key));
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "No se encontró fichero de usuarios, se creará uno nuevo");
        }
        return usuarios;
    }

    public void guardarMensaje(String salon, Mensaje mensaje) {
        String fichero = DIRECTORIO_SALONES + salon + ".txt";
        try (FileWriter fw = new FileWriter(fichero, true)) {
            fw.write(mensaje.getTimestamp() + MtpaProtocolo.DELIMITADOR_ENVIO + mensaje.getUsername() + MtpaProtocolo.DELIMITADOR_ENVIO + mensaje.getContenido() + "\n");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar mensaje en salón " + salon, e);
        }
    }

    public ArrayList<Mensaje> cargarMensajes(String salon, LocalDate fecha) {
        ArrayList<Mensaje> mensajes = new ArrayList<>();
        String fichero = DIRECTORIO_SALONES + salon + ".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(fichero))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] fragmentos = linea.split(MtpaProtocolo.DELIMITADOR);
                if (fragmentos.length == 3) {
                    LocalDateTime timestamp = LocalDateTime.parse(fragmentos[0]);
                    if (timestamp.toLocalDate().equals(fecha)) {
                        mensajes.add(new Mensaje(fragmentos[1], fragmentos[2]));
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "No se encontró fichero del salón " + salon);
        }
        return mensajes;
    }
}
