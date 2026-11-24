package mx.uady.boutique.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.dao.UsuarioDAO;
import mx.uady.boutique.exception.CorreoDuplicadoException;
import mx.uady.boutique.exception.CredencialesInvalidasException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de autenticación.
 *
 * Verifica las reglas de negocio correspondientes al registro
 * de usuarios (RF-01) y al inicio de sesión (RF-02).
 *
 * Incluye casos normales, casos límite y casos de error.
 * Utiliza un DAO en memoria para evitar la dependencia
 * de una conexión real a la base de datos.
 */
class AutenticacionServiceTest {

    /** DAO en memoria para probar el servicio sin base de datos. */
    private static class UsuarioDAOEnMemoria extends UsuarioDAO {
        final List<Usuario> usuarios = new ArrayList<>();

        @Override
        public boolean existeCorreo(String correo) {
            return usuarios.stream().anyMatch(u -> u.getCorreo().equals(correo));
        }

        @Override
        public boolean insertar(Usuario usuario) {
            usuario.setIdUsuario(usuarios.size() + 1);
            usuarios.add(usuario);
            return true;
        }

        @Override
        public Usuario buscarPorCorreo(String correo) {
            return usuarios.stream().filter(u -> u.getCorreo().equals(correo)).findFirst().orElse(null);
        }
    }

    private UsuarioDAOEnMemoria dao;
    private AutenticacionService servicio;
    
     /**
     * Inicializa un DAO y un servicio nuevos antes de cada prueba.
     * Garantiza que las pruebas sean independientes y que sus
     * resultados no dependan del orden de ejecución.
     */
    @BeforeEach
    void preparar() {
        dao = new UsuarioDAOEnMemoria();
        servicio = new AutenticacionService(dao);
    }

    /**
     * Tipo de caso: NORMAL.
     * Requisito: RF-01.
     *
     * Verifica que un usuario con datos válidos pueda registrarse
     * correctamente, incluso si su nombre y correo contienen
     * espacios al inicio o al final.
     *
     * Resultado esperado: los datos se almacenan sin espacios
     * innecesarios y la contraseña no se guarda en texto plano.
     */
    @Test
    void registraUsuarioConContrasenaCifradaYDatosLimpios() throws Exception {
        servicio.registrar("  Ana ", "Pérez", " ana@uady.mx ", "", "clave1234", "clave1234");

        Usuario guardado = dao.usuarios.get(0);
        assertEquals("Ana", guardado.getNombre());
        assertEquals("ana@uady.mx", guardado.getCorreo());
        assertNotEquals("clave1234", guardado.getPasswordHash());
    }

    /**
     * Tipo de caso: ERROR.
     * Requisito: RF-01.
     *
     * Verifica que el sistema impida registrar dos usuarios
     * con el mismo correo electrónico.
     *
     * Resultado esperado: se lanza CorreoDuplicadoException
     * con el mensaje de error correspondiente.
     */
    @Test
    void correoDuplicadoLanzaExcepcion() throws Exception {
        servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "clave1234");

        CorreoDuplicadoException e = assertThrows(CorreoDuplicadoException.class,
                () -> servicio.registrar("Luis", "Díaz", "ana@uady.mx", "", "otra1234", "otra1234"));
        assertEquals(AutenticacionService.MSG_CORREO_DUPLICADO, e.getMessage());
    }

    /**
     * Tipo de caso: ERROR.
     * Requisito: RF-01.
     *
     * Verifica que el sistema rechace el registro cuando
     * la contraseña y su confirmación son diferentes.
     *
     * Resultado esperado: se lanza ValidacionException.
     */
    @Test
    void registroConContrasenasDistintasLanzaValidacion() {
        assertThrows(ValidacionException.class,
                () -> servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "distinta"));
    }

    /**
     * Tipo de caso: NORMAL.
     * Requisito: RF-02.
     *
     * Verifica que un usuario previamente registrado pueda
     * iniciar sesión utilizando credenciales correctas.
     *
     * Resultado esperado: el servicio devuelve al usuario
     * correspondiente con un identificador válido.
     */
    @Test
    void inicioDeSesionCorrectoDevuelveUsuario() throws Exception {
        servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "clave1234");

        Usuario u = servicio.iniciarSesion("ana@uady.mx", "clave1234");
        assertEquals("Ana", u.getNombre());
        assertTrue(u.getIdUsuario() > 0);
    }

    /**
     * Tipo de caso: ERROR.
     * Requisito: RF-02.
     *
     * Verifica que el sistema rechace el inicio de sesión
     * cuando el correo existe, pero la contraseña es incorrecta.
     *
     * Resultado esperado: se lanza CredencialesInvalidasException
     * con el mensaje correspondiente a una contraseña incorrecta.
     */
    @Test
    void contrasenaIncorrectaLanzaCredencialesInvalidas() throws Exception {
        servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "clave1234");

        CredencialesInvalidasException e = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("ana@uady.mx", "mala"));
        assertEquals(AutenticacionService.MSG_PASSWORD_INCORRECTA, e.getMessage());
    }

    /**
     * Tipo de caso: ERROR.
     * Requisito: RF-02.
     *
     * Verifica que el sistema rechace el inicio de sesión
     * cuando el correo electrónico no está registrado.
     *
     * Resultado esperado: se lanza CredencialesInvalidasException
     * con el mensaje correspondiente a una cuenta inexistente.
     */
    @Test
    void correoInexistenteLanzaCredencialesInvalidas() {
        CredencialesInvalidasException e = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("nadie@uady.mx", "clave1234"));
        assertEquals(AutenticacionService.MSG_SIN_CUENTA, e.getMessage());
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR.
     * Requisito: RF-02.
     *
     * Evalúa el comportamiento del servicio cuando los campos
     * obligatorios tienen una longitud de cero caracteres.
     *
     * Resultado esperado: se rechaza el inicio de sesión
     * y se lanza ValidacionException.
     */
    @Test
    void loginConCamposVaciosLanzaValidacion() {
        assertThrows(ValidacionException.class, () -> servicio.iniciarSesion("", ""));
    }

    /**
     * Tipo de caso: LÍMITE.
     * Requisito: RF-01.
     *
     * Verifica que el registro acepte una contraseña de exactamente
     * ocho caracteres, correspondiente a la longitud mínima
     * establecida en la especificación de requisitos.
     *
     * Resultado esperado: el usuario se registra correctamente.
     */
    @Test
    void registroConContrasenaDeOchoCaracteresEsValido()
        throws Exception {
            servicio.registrar("Ana", "Pérez", "ana@uady.mx","", "clave123", "clave123");
            assertEquals(1, dao.usuarios.size());
            assertEquals("ana@uady.mx", dao.usuarios.get(0).getCorreo());
            assertNotEquals("clave123", dao.usuarios.get(0).getPasswordHash());
        }
}
