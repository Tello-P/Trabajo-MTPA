package mtpa.gestores;

import mtpa.excepciones.MtpaExcepcion;
import mtpa.modelo.Mensaje;
import mtpa.protocolo.MtpaProtocolo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Gestiona los salones del sistema: validación, mensajes e historial.
 * Los 5 salones son fijos y predefinidos, no se cargan en memoria porque siempre serán los mismos,
 * independietemente del momento de la ejecución del código
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorSalones {

    private static final String DIRECTORIO_SALONES = "salones/";
    private static final int MAX_USUARIOS_SALON = 15;

    //Salones predefinidos del sistema, siempre son estos 5
    private static final String[] NOMBRES_SALONES = {"IA", "Deportes", "Therian", "Manga", "UEMC"};

    public GestorSalones() {
        //Se crea la carpeta de salones si no existe
        new File(DIRECTORIO_SALONES).mkdirs();
    }

    //Comprueba si el nombre del salón es válido, lanza excepción si no existe
    public void validarSalon(String nombre) throws MtpaExcepcion {
        for (String s : NOMBRES_SALONES) {
            if (s.equals(nombre)) {
                return;
            }
        }
        //Si no se encuentra se lanza excepción
        throw new MtpaExcepcion(MtpaExcepcion.SALON_NO_VALIDO);
    }

    //Guarda el mensaje en el fichero del salón correspondiente
    //Cada salón tiene su propio fichero
    //Se guarda con un formato especifico definido
    public Mensaje guardarMensaje(String salon, String username, String contenido) throws MtpaExcepcion {
        validarSalon(salon);
        Mensaje mensaje = new Mensaje(username, contenido);
        String fichero = DIRECTORIO_SALONES + salon + ".txt"; //ubicacion del fichero
        try (FileWriter fw = new FileWriter(fichero, true)) {
            //El fomato de guardado es FECHA|MENSAJE|CONTENIDO
            fw.write(mensaje.getTimestamp() + MtpaProtocolo.DELIMITADOR_ENVIO + mensaje.getUsername() + MtpaProtocolo.DELIMITADOR_ENVIO + mensaje.getContenido() + "\n");
        } catch (Exception e) {
            GestorLogs.error("SISTEMA", "GUARDAR_MENSAJE", "Error al guardar mensaje en salón " + salon + ": " + e.getMessage());
        }
        GestorLogs.info(username, "SEND_ROOM", "Mensaje en salón " + salon);
        return mensaje;
    }

    //Devuelve los mensajes de un salón en una fecha concreta que es definida como parametro
    //Se devuelve una lista de mensajes
    public ArrayList<Mensaje> getHistorial(String salon, LocalDate fecha) throws MtpaExcepcion {
        validarSalon(salon); //valida existencia
        ArrayList<Mensaje> mensajes = new ArrayList<>(); //Se definde arraylist de mensajes
        String fichero = DIRECTORIO_SALONES + salon + ".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(fichero))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                //Se parte la linea por | y se crea el objeto Mensaje
                String[] fragmentos = linea.split(MtpaProtocolo.DELIMITADOR);
                LocalDateTime timestamp = LocalDateTime.parse(fragmentos[0]);
                //condicional de fecha buscada
                if (timestamp.toLocalDate().equals(fecha)) {
                    mensajes.add(new Mensaje(fragmentos[1], fragmentos[2]));
                }
            }
        } catch (Exception e) {
            GestorLogs.info("SISTEMA", "CARGAR_MENSAJES", "No se encontró fichero del salón: " + salon);
        }
        return mensajes;
    }

    //Devuelve los nombres de todos los salones disponibles separados por coma
    //Este método es necesario para mandárselo al cliente cuando se inicia sesión
    //este debe de saber que salones existen, y no debería de almacenar el esos salones
    public String listarSalones() {
        String resultado = "";
        for (String nombre : NOMBRES_SALONES) {
            resultado = resultado + nombre + ",";
        }
        //Se quita la ultima coma
        return resultado.substring(0, resultado.length() - 1);
    }
}
