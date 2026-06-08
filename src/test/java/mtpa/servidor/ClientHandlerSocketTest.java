package mtpa.servidor;

import mtpa.gestores.GestorSalones;
import mtpa.gestores.GestorUsuarios;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del ClientHandler usando una conexión real por socket.
 * Se levanta un servidor de prueba, se conecta un cliente y se comprueban las respuestas.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ClientHandlerSocketTest {

    public ClientHandlerSocketTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    //Se genera una conexion por socket para testear el paso de mensjaes 
    //Metodo ayudante que abre un servidor de prueba, manda un comando y devuelve la respuesta
    private String enviarComando(String comando) throws Exception {
        ServerSocket servidor = new ServerSocket(0);
        int puerto = servidor.getLocalPort();
        GestorUsuarios gestorUsuarios = new GestorUsuarios();
        GestorSalones gestorSalones = new GestorSalones();
        ArrayList<ClientHandler> clientes = new ArrayList<>();

        //Hilo que acepta la conexion y arranca el handler del cliente
        Thread aceptador = new Thread(() -> {
            try {
                Socket conexion = servidor.accept();
                ClientHandler handler = new ClientHandler(conexion, gestorUsuarios, gestorSalones, clientes);
                handler.start();
            } catch (Exception e) {
            }
        });
        aceptador.start();

        //El cliente se conecta, manda el comando y lee la primera respuesta
        Socket cliente = new Socket("localhost", puerto);
        cliente.getOutputStream().write((comando + "\n").getBytes("UTF-8"));
        BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
        String respuesta = br.readLine();

        cliente.close();
        servidor.close();
        return respuesta;
    }

    //Comprobamos que al registrar un usuario por la conexion el servidor responde con un REGISTER_OK
    @Test
    public void testRegistro01OK() throws Exception {
        String nombre = "socket" + System.nanoTime();
        String respuesta = enviarComando("REGISTER|" + nombre);
        boolean esperado = true;
        boolean resultado = respuesta.startsWith("REGISTER_OK");
        assertEquals(resultado, esperado);
    }

    //Comprobamos que si mandamos un comando que no existe el servidor nos devuelve un error
    @Test
    public void testComandoDesconocido01KO() throws Exception {
        String respuesta = enviarComando("INVENTADO|cosa");
        boolean esperado = true;
        boolean resultado = respuesta.startsWith("ERROR");
        assertEquals(resultado, esperado);
    }
}
