package mtpa.servidor;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del ClientHandler.
 * Se comprueba el estado inicial de un cliente recién creado.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ClientHandlerTest {

    public ClientHandlerTest() {
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

    //Cuando creamos un cliente nuevo todavia no ha hecho login asi que su nombre esta vacio
    @Test
    public void testGetUsername01OK() {
        ClientHandler cliente = new ClientHandler(null, null, null, null);
        String esperado = null;
        String resultado = cliente.getUsername();
        assertEquals(resultado, esperado);
    }

    //Cuando creamos un cliente nuevo todavia no ha entrado a ningun salon
    @Test
    public void testGetSalonActual01OK() {
        ClientHandler cliente = new ClientHandler(null, null, null, null);
        String esperado = null;
        String resultado = cliente.getSalonActual();
        assertEquals(resultado, esperado);
    }
}
