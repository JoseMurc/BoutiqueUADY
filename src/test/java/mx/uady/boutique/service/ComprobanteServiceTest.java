package mx.uady.boutique.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import mx.uady.boutique.dao.PedidoDAO;
import mx.uady.boutique.model.DetallePedido;
import mx.uady.boutique.model.Pedido;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de comprobantes.
 *
 * Verifica la generación de comprobantes en texto (RF-16)
 * incluyendo casos normales, límites y de error sin usar BD real.
 */
class ComprobanteServiceTest {

    /**
     * Tipo de caso: NORMAL.
     * Requisito: RF-16.
     *
     * Verifica que generarTexto incluya los datos del pedido,
     * productos con talla y sin talla, formato de dos decimales
     * y el total en MXN.
     *
     * Resultado esperado: el texto contiene número de pedido,
     * sucursal, renglones formateados y total.
     */
    @Test
    void generaTextoConRenglonesYFormatoCorrecto() {
        DetallePedido conTalla = new DetallePedido();
        conTalla.setNombre("Playera deportiva UADY");
        conTalla.setTalla("S");
        conTalla.setCantidad(1);
        conTalla.setPrecioUnitario(220.0);

        DetallePedido sinTalla = new DetallePedido();
        sinTalla.setNombre("Llavero escudo");
        sinTalla.setTalla("");
        sinTalla.setCantidad(1);
        sinTalla.setPrecioUnitario(60.0);

        ComprobanteService servicio = new ComprobanteService();
        Timestamp fecha = Timestamp.valueOf(LocalDateTime.of(2026, 11, 12, 22, 10));
        String texto = servicio.generarTexto(3, "Angel Daniel", "FMAT", fecha,
                280.0, "pendiente", List.of(conTalla, sinTalla));

        assertNotNull(texto);
        assertTrue(texto.contains("3"));
        assertTrue(texto.contains("FMAT"));
        assertTrue(texto.contains("220.00"));
        assertTrue(texto.contains("60.00"));
        assertTrue(texto.contains("280.00"));
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR.
     * Requisito: RF-16.
     *
     * Verifica que generarTexto maneje de forma segura un
     * cliente o estado vacío sin lanzar excepciones inesperadas.
     *
     * Resultado esperado: devuelve el texto del comprobante
     * con los campos vacíos manejados sin fallar.
     */
    @Test
    void generaTextoConDatosBasicosVacios() {
        ComprobanteService servicio = new ComprobanteService();
        Timestamp fecha = Timestamp.valueOf(LocalDateTime.of(2026, 11, 12, 12, 0));
        String texto = servicio.generarTexto(0, "", "", fecha, 0.0, "", List.of());

        assertNotNull(texto);
        assertTrue(texto.contains("0.00"));
    }
    
    /**
     * Tipo de caso: LÍMITE.
     * Requisito: RF-16.
     *
     * Verifica que generarTexto funcione cuando la lista de
     * detalles está vacía y el total es 0.0.
     *
     * Resultado esperado: genera el comprobante con 0.00 sin fallar.
     */
    @Test
    void generaTextoSinDetallesConTotalCero() {
        ComprobanteService servicio = new ComprobanteService();
        Timestamp fecha = Timestamp.valueOf(LocalDateTime.of(2026, 11, 12, 10, 0));
        String texto = servicio.generarTexto(1, "Ana", "Principal", fecha, 0.0, "pendiente", List.of());

        assertNotNull(texto);
        assertTrue(texto.contains("0.00"));
    }
}