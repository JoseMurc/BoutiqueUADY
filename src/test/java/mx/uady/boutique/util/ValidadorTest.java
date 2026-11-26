package mx.uady.boutique.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uady.boutique.exception.ValidacionException;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la clase de utilidad Validador
 *
 * Verifica las reglas de validación y limpieza de datos
 * correspondientes al registro de usuarios y al inicio de sesión
 *
 * Requisitos asociados: RF-01 y RF-02
 *
 * Incluye casos normales, casos límite y casos de error
 * para comprobar la detección de campos vacíos, el manejo
 * de valores nulos, la eliminación de espacios innecesarios
 * y la validación de los datos de autenticación
 *
 * Las pruebas son independientes entre sí y no requieren
 * una conexión a la base de datos
 *
 * @see Validador
 * @see ValidacionException
 */
class ValidadorTest {

    /**
     * Tipo de caso: NORMAL Y LÍMITE
     * Requisitos: RF-01 y RF-02
     *
     * Verifica que el método estaVacio identifique correctamente
     * si un campo carece de información
     *
     * Se evalúan tres situaciones:
     * Un valor nulo
     * Una cadena vacía de longitud cero
     * Una cadena que contiene información válida
     *
     * Resultado esperado: el método devuelve true para los
     * valores nulos y las cadenas vacías, y false para las
     * cadenas que contienen información
     */
    @Test
    void detectaCamposVacios() {
        assertTrue(Validador.estaVacio(null));
        assertTrue(Validador.estaVacio(""));
        assertFalse(Validador.estaVacio("Ana"));
    }

    /**
     * Tipo de caso: NORMAL Y LÍMITE
     * Requisitos: RF-01 y RF-02
     *
     * Verifica que el método limpiar elimine los espacios
     * innecesarios al inicio y al final de una cadena
     *
     * También comprueba el comportamiento del método cuando
     * recibe un valor nulo como entrada
     *
     * Resultado esperado: la cadena conserva su contenido
     * sin espacios externos y los valores nulos se convierten
     * en cadenas vacías
     */
    @Test
    void limpiarQuitaEspaciosYConvierteNuloEnVacio() {
        assertEquals("Ana", Validador.limpiar("  Ana  "));
        assertEquals("", Validador.limpiar(null));
    }

     /**
     * Tipo de caso: LÍMITE Y ERROR
     * Requisito: RF-01
     *
     * Verifica que el sistema rechace los datos de registro
     * cuando uno de los campos obligatorios está vacío
     *
     * Se proporciona un nombre de longitud cero, mientras
     * que los demás campos contienen información válida
     *
     * Resultado esperado: se lanza ValidacionException
     * con el mensaje correspondiente a la ausencia de
     * campos obligatorios
     */
    @Test
    void registroConCampoObligatorioVacioFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarRegistro("", "Pérez", "a@b.com", "12345678", "12345678"));
        assertEquals(Validador.MSG_CAMPOS_OBLIGATORIOS, e.getMessage());
    }

    /**
     * Tipo de caso: ERROR
     * Requisito: RF-01
     *
     * Verifica que el sistema rechace los datos de registro
     * cuando la contraseña y su confirmación son diferentes
     *
     * Se proporcionan dos contraseñas de ocho caracteres
     * con contenido distinto, manteniendo válidos los demás
     * campos del registro
     *
     * Resultado esperado: se lanza ValidacionException
     * con el mensaje correspondiente a contraseñas distintas
     */
    @Test
    void registroConContrasenasDistintasFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarRegistro("Ana", "Pérez", "a@b.com", "12345678", "87654321"));
        assertEquals(Validador.MSG_PASSWORDS_DISTINTAS, e.getMessage());
    }

    /**
     * Tipo de caso: NORMAL
     * Requisito: RF-01
     *
     * Verifica que el método validarRegistro acepte los datos
     * de un usuario cuando todos los campos obligatorios
     * contienen información y las contraseñas coinciden
     *
     * Se utiliza una contraseña de ocho caracteres,
     * correspondiente a la longitud mínima establecida
     * en la especificación de requisitos
     *
     * Resultado esperado: el método finaliza correctamente
     * sin lanzar ninguna excepción de validación
     *
     * @throws ValidacionException si el método rechaza
     *         inesperadamente los datos válidos del registro
     */
    @Test
    void registroValidoNoLanzaExcepcion() throws ValidacionException {
        Validador.validarRegistro("Ana", "Pérez", "a@b.com", "12345678", "12345678");
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR
     * Requisito: RF-02
     *
     * Verifica que el sistema rechace los datos de inicio
     * de sesión cuando uno de los campos obligatorios
     * no contiene información
     *
     * Se proporciona un correo electrónico válido y una
     * contraseña vacía de longitud cero
     *
     * Resultado esperado: se lanza ValidacionException
     * con el mensaje correspondiente a los campos vacíos
     * del inicio de sesión
     */
    @Test
    void loginVacioFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarLogin("a@b.com", ""));
        assertEquals(Validador.MSG_LOGIN_VACIO, e.getMessage());
    }

    /**
     * Tipo de caso: NORMAL
     * Requisitos: RF-01 y RF-02
     *
     * Verifica que el método limpiar conserve el contenido
     * de una cadena que no presenta espacios innecesarios
     *
     * Resultado esperado: la cadena permanece sin modificaciones
     */
    @Test
    void limpiarConservaCadenaValida() {
        assertEquals("Ana", Validador.limpiar("Ana"));
    }
    
    /**
     * Tipo de caso: LÍMITE.
     * Requisitos: RF-01 y RF-02.
     *
     * Verifica el comportamiento de estaVacio y limpiar cuando
     * se recibe una cadena compuesta solo por espacios.
     *
     * Resultado esperado: limpiar devuelve cadena vacía y al
     * evaluarla con estaVacio se detecta como vacía.
     */
    @Test
    void cadenaConSoloEspaciosQuedaVaciaAlLimpiar() {
        String limpia = Validador.limpiar("    ");
        assertEquals("", limpia);
        assertTrue(Validador.estaVacio(limpia));
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR.
     * Requisito: RF-02.
     *
     * Verifica que validarLogin rechace cuando el correo está
     * vacío aunque la contraseña tenga contenido.
     *
     * Resultado esperado: se lanza ValidacionException con
     * MSG_LOGIN_VACIO.
     */
    @Test
    void loginConCorreoVacioFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarLogin("", "clave1234"));
        assertEquals(Validador.MSG_LOGIN_VACIO, e.getMessage());
    }
}
