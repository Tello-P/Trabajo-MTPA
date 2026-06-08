package mtpa.protocolo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias de MtpaPeticion.
 * Se comprueba que el mensaje recibido se parte correctamente en comando y parámetros.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class MtpaPeticionTest {

    public MtpaPeticionTest() {
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

    //Comprobamos que al construir una peticion de registro el comando que saca es REGISTER
    @Test
    public void testGetComando01OK() {
        MtpaPeticion peticion = MtpaPeticion.construir("REGISTER|pepe");
        String esperado = "REGISTER";
        String resultado = peticion.getComando();
        assertEquals(resultado, esperado);
    }

    //Comprobamos que al construir una peticion de login el comando que saca es LOGIN
    @Test
    public void testGetComando02OK() {
        MtpaPeticion peticion = MtpaPeticion.construir("LOGIN|pepe|5");
        String esperado = "LOGIN";
        String resultado = peticion.getComando();
        assertEquals(resultado, esperado);
    }

    //Miramos que una peticion de login con su usuario y su clave tiene dos parametros
    @Test
    public void testTotalParametros01OK() {
        MtpaPeticion peticion = MtpaPeticion.construir("LOGIN|pepe|5");
        int esperado = 2;
        int resultado = peticion.totalParametros();
        assertEquals(resultado, esperado);
    }

    //Comprobamos que el primer parametro de un login es el nombre de usuario
    @Test
    public void testGetParametro01OK() {
        MtpaPeticion peticion = MtpaPeticion.construir("LOGIN|pepe|5");
        String esperado = "pepe";
        String resultado = peticion.getParametro(0);
        assertEquals(resultado, esperado);
    }
}
