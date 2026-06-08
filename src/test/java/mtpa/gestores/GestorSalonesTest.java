package mtpa.gestores;

import mtpa.excepciones.MtpaExcepcion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del GestorSalones.
 * Se comprueba que la validación de salones funciona correctamente.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorSalonesTest {

    public GestorSalonesTest() {
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

    //Miramos que un salon que existe de verdad se valida correctamente y deja pasar
    @Test
    public void testValidarSalon01OK() {
        GestorSalones gestor = new GestorSalones();
        boolean esperado = true;
        boolean resultado;
        try {
            gestor.validarSalon("IA");
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }

    //Probamos con un salon inventado para confirmar que el sistema lo bloquea
    @Test
    public void testValidarSalon02KO() {
        GestorSalones gestor = new GestorSalones();
        boolean esperado = false;
        boolean resultado;
        try {
            gestor.validarSalon("SalonInventado");
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }
}
