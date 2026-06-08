package mtpa.gestores;

import java.io.BufferedReader;
import java.io.FileReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del GestorLogs.
 * Se comprueba que las operaciones quedan registradas en el fichero de logs.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorLogsTest {

    public GestorLogsTest() {
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

    //Comprobamos que cuando ocurre algo en el servidor se escribe la linea en el fichero de logs
    @Test
    public void testInfo01OK() {
        boolean esperado = true;
        boolean resultado = false;
        String detalle = "pruebaDeLog" + System.nanoTime();
        GestorLogs.info("TEST", "PRUEBA", detalle);
        try (BufferedReader br = new BufferedReader(new FileReader("servidor.log"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains(detalle)) {
                    resultado = true;
                }
            }
        } catch (Exception e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }
}
