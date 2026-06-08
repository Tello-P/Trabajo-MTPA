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
        String entrada = "REGISTER|alejandro";
        boolean resultado;
        try {
            validador.validar(entrada);
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }

    //Aqui comprobamos que un mensaje que no se pasa del limite de caracteres se acepta sin problemas
    @Test
    public void testValidar02OK() {
        Validador validador = new Validador();
        boolean esperado = true;
        String entrada = "SEND_ROOM|IA|hola que tal estais";
        boolean resultado;
        try {
            validador.validar(entrada);
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }

    //Aqui forzamos un mensaje mas largo de la cuenta para asegurarnos de que el validador lo rechaza
    @Test
    public void testValidar03KO() {
        Validador validador = new Validador();
        boolean esperado = false;
        String contenido = "a".repeat(200);
        String entrada = "SEND_ROOM|IA|" + contenido;
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
