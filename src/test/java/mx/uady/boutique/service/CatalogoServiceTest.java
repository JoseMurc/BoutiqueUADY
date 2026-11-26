package mx.uady.boutique.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de catálogo.
 *
 * Verifica el filtrado por sucursal y talla (RF-06, RF-07)
 * y la consulta de detalle (RF-09) sin modificar el código
 * de producción.
 */
class CatalogoServiceTest {

    /**
     * Tipo de caso: LÍMITE.
     * Requisitos: RF-06 y RF-07.
     *
     * Verifica que filtrar sin filtros de sucursal ni talla
     * devuelva una lista no nula sin lanzar errores.
     *
     * Resultado esperado: devuelve una lista de productos no nula.
     */
    @Test
    void sinFiltrosDevuelveListaNoNula() throws SQLException {
        CatalogoService servicio = new CatalogoService();
        List<mx.uady.boutique.model.Producto> resultado = servicio.filtrar("", "");

        assertNotNull(resultado);
    }

    /**
     * Tipo de caso: ERROR.
     * Requisito: RF-09.
     *
     * Verifica que detalle devuelva null cuando el identificador
     * del producto no existe.
     *
     * Resultado esperado: devuelve null.
     */
    @Test
    void detalleDeProductoInexistenteDevuelveNull() throws SQLException {
        CatalogoService servicio = new CatalogoService();
        assertNull(servicio.detalle(999999));
    }
}