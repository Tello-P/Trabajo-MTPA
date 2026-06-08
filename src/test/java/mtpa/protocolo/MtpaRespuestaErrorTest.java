package mtpa.protocolo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias de MtpaRespuestaError.
 * Se comprueba que la respuesta de error se forma con el texto esperado.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class MtpaRespuestaErrorTest {

    public MtpaRespuestaErrorTest() {
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

    //Comprobamos que una respuesta de error genera el texto completo tal y como lo espera el cliente
    @Test
    public void testToString01OK() {
        MtpaRespuestaError error = new MtpaRespuestaError("ERR_001", "Error de prueba");
        String esperado = "ERROR|ERR_001|Error de prueba\n";
        String resultado = error.toString();
        assertEquals(resultado, esperado);
    }
}
