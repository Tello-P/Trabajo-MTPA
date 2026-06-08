package mtpa.gestores;

import mtpa.excepciones.MtpaExcepcion;
import mtpa.modelo.Usuario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del GestorUsuarios.
 * Se comprueba el registro de usuarios y el inicio de sesión.
 *
 * @author Alejandro Franco González & Tello Pérez Garrote
 */
public class GestorUsuariosTest {

    public GestorUsuariosTest() {
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

    //Comprobamos que cuando registramos un usuario nuevo el servidor nos devuelve una clave valida
    @Test
    public void testRegistrar01OK() {
        GestorUsuarios gestor = new GestorUsuarios();
        boolean esperado = true;
        boolean resultado;
        try {
            String nombre = "registroOk" + System.nanoTime();
            Usuario usuario = gestor.registrar(nombre);
            resultado = (usuario != null && usuario.getKey() > 0);
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }

    //Aqui miramos que si intentamos registrar dos veces el mismo usuario el sistema no nos deja
    @Test
    public void testRegistrar02KO() {
        GestorUsuarios gestor = new GestorUsuarios();
        boolean esperado = false;
        boolean resultado;
        try {
            String nombre = "registroKo" + System.nanoTime();
            gestor.registrar(nombre);
            gestor.registrar(nombre);
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }

    //Probamos que entrando con la clave correcta el usuario consigue iniciar sesion sin problemas
    @Test
    public void testLogin01OK() {
        GestorUsuarios gestor = new GestorUsuarios();
        boolean esperado = true;
        boolean resultado;
        try {
            String nombre = "loginOk" + System.nanoTime();
            Usuario usuario = gestor.registrar(nombre);
            Usuario logueado = gestor.login(nombre, usuario.getKey());
            resultado = (logueado != null);
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }

    //Vemos que si metemos una clave que no es la suya el login falla como tiene que fallar
    @Test
    public void testLogin02KO() {
        GestorUsuarios gestor = new GestorUsuarios();
        boolean esperado = false;
        boolean resultado;
        try {
            String nombre = "loginKo" + System.nanoTime();
            Usuario usuario = gestor.registrar(nombre);
            gestor.login(nombre, usuario.getKey() + 1);
            resultado = true;
        } catch (MtpaExcepcion e) {
            resultado = false;
        }
        assertEquals(resultado, esperado);
    }
}
