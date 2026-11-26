package mx.uady.boutique.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la clase de utilidad para la generación
 * y verificación de hashes de contraseñas
 *
 * Verifica que las contraseñas no se almacenen en texto plano,
 * que puedan verificarse correctamente y que se rechacen
 * contraseñas incorrectas o hashes inválidos
 *
 * También comprueba la compatibilidad con hashes que utilizan
 * el prefijo de BCrypt empleado por PHP
 *
 * Requisitos asociados: RF-01 y RF-02
 *
 * Incluye casos normales, casos límite y casos de error
 * relacionados con la seguridad y validación de contraseñas
 *
 * @see PasswordUtil
 */
class PasswordUtilTest {

    /**
     * Tipo de caso: NORMAL
     * Requisitos: RF-01 y RF-02
     *
     * Verifica que el servicio genere un hash a partir de una
     * contraseña válida y que posteriormente pueda comprobar
     * que la contraseña original corresponde al hash generado
     *
     * Resultado esperado: el hash generado es diferente de la
     * contraseña original y el método verificar devuelve true
     * cuando se proporciona la contraseña correcta
     */
    @Test
    void cifrarYVerificarContrasenaCorrecta() {
        String hash = PasswordUtil.cifrar("miClave123");
        assertNotEquals("miClave123", hash);
        assertTrue(PasswordUtil.verificar("miClave123", hash));
    }

    /**
     * Tipo de caso: ERROR
     * Requisito: RF-02
     *
     * Verifica que el sistema rechace una contraseña que no
     * corresponde al hash generado originalmente
     *
     * Se genera un hash utilizando una contraseña válida y
     * posteriormente se intenta verificar con una diferente
     *
     * Resultado esperado: el método verificar devuelve false,
     * indicando que la contraseña proporcionada es incorrecta
     */
    @Test
    void rechazaContrasenaIncorrecta() {
        String hash = PasswordUtil.cifrar("miClave123");
        assertFalse(PasswordUtil.verificar("otraClave", hash));
    }

     /**
     * Tipo de caso: NORMAL
     * Requisito: RF-02
     *
     * Verifica que el método de verificación sea compatible
     * con hashes BCrypt que utilizan el prefijo $2y$,
     * empleado habitualmente por PHP
     *
     * Para simular este formato, se genera un hash en Java
     * y se reemplaza su prefijo original por $2y$
     *
     * Resultado esperado: el método verificar reconoce
     * correctamente el formato del hash y devuelve true
     * al proporcionar la contraseña original
     */
    @Test
    void aceptaHashGeneradoPorPhpConPrefijo2y() {
        String hashJava = PasswordUtil.cifrar("secreto88");
        String comoPhp = "$2y$" + hashJava.substring(4);
        assertTrue(PasswordUtil.verificar("secreto88", comoPhp));
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR
     * Requisito: RF-02
     *
     * Verifica que el método de verificación maneje de forma
     * segura las entradas inválidas y los valores nulos
     *
     * Se evalúan las siguientes situaciones:
     *
     * Una contraseña con un hash de formato inválido
     * Una contraseña con valor null
     * Un hash con valor null
     *
     * Los valores nulos representan condiciones extremas
     * relacionadas con la ausencia de datos de entrada
     *
     * Resultado esperado: el método verificar devuelve false
     * en todos los casos, sin propagar excepciones inesperadas
     */
    @Test
    void hashInvalidoONuloNoVerifica() {
        assertFalse(PasswordUtil.verificar("x", "no-es-un-hash"));
        assertFalse(PasswordUtil.verificar(null, "hash"));
        assertFalse(PasswordUtil.verificar("x", null));
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR
     * Requisito: RF-02
     *
     * Verifica el comportamiento del método verificar cuando
     * recibe una contraseña de longitud igual a cero
     *
     * Resultado esperado: el método devuelve false, debido a
     * que la contraseña vacía no corresponde al hash generado
     */
    @Test
    void contrasenaVaciaNoVerifica() {
        String hash = PasswordUtil.cifrar("miClave123");
        assertFalse(PasswordUtil.verificar("", hash));
    }
    
    /**
     * Tipo de caso: LÍMITE.
     * Requisitos: RF-01 y RF-02.
     *
     * Verifica que cifrar la misma contraseña dos veces genere
     * hashes distintos (por el salt de BCrypt), pero que ambos
     * sigan siendo válidos al verificar.
     *
     * Resultado esperado: los dos hashes son distintos y ambos
     * devuelven true en verificar.
     */
    @Test
    void cifrarMismaClaveDosVecesGeneraHashesDistintosValidos() {
        String hash1 = PasswordUtil.cifrar("miClave123");
        String hash2 = PasswordUtil.cifrar("miClave123");

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.verificar("miClave123", hash1));
        assertTrue(PasswordUtil.verificar("miClave123", hash2));
    }
}
