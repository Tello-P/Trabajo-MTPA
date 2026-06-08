package mtpa.servidor;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del Servidor.
 * Se comprueba el estado inicial de los modos de administración.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ServidorTest {

    public ServidorTest() {
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

    //Comprobamos que nada mas arrancar el servidor no esta en modo mantenimiento
    @Test
    public void testMantenimiento01OK() {
        boolean esperado = false;
        boolean resultado = Servidor.isMantenimiento();
        assertEquals(resultado, esperado);
    }

    //Comprobamos que nada mas arrancar el servidor no esta bloqueado y acepta clientes
    @Test
    public void testBloqueado01OK() {
        boolean esperado = false;
        boolean resultado = Servidor.isBloqueado();
        assertEquals(resultado, esperado);
    }
}
