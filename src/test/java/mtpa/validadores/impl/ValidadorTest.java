package mtpa.validadores.impl;

import mtpa.excepciones.MtpaExcepcion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del Validador del protocolo MTPA.
 * Se aplican clases de equivalencia sobre el método validar.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class ValidadorTest {

    public ValidadorTest() {
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

    //Clase válida: comando REGISTER con su parámetro, no debe lanzar excepción
    @Test
    public void testValidar01OK() {
        Validador validador = new Validador();
        boolean esperado = true;
        String entrada = "REGISTER|pepe";
        boolean resultado;
        try {
            validador.validar(entrada);
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }
}
