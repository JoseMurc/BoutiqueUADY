package mx.uady.boutique.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.uady.boutique.dao.CarritoDAO;
import mx.uady.boutique.model.ItemCarrito;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del servicio de carrito de compras
 *
 * Verifica las reglas de negocio correspondientes a la adición,
 * validación, agrupación y almacenamiento de productos en el carrito
 *
 * Requisitos asociados: RF-10, RF-11 y RF-12
 *
 * Incluye casos normales, casos límite y casos de error para
 * comprobar que el servicio procesa correctamente los datos
 * recibidos y descarta aquellos que no cumplen las reglas
 * de negocio
 *
 * @see CarritoService
 * @see CarritoDAO
 * @see ItemCarrito
 */
class CarritoServiceTest {

    private static Map<String, Object> producto(Object id, Object talla, Object nombre,
                                                Object precio, Object cantidad) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("talla", talla);
        m.put("nombre", nombre);
        m.put("precio", precio);
        m.put("imagen", "fotos boutique/x.jpg");
        m.put("cantidad", cantidad);
        return m;
    }

    /**
     * Tipo de caso: NORMAL
     * Requisitos: RF-10 y RF-12
     *
     * Verifica que el servicio acepte un producto con datos
     * válidos, una talla permitida, un precio positivo y una
     * cantidad mayor o igual a uno
     *
     * Resultado esperado: se obtiene una línea del carrito
     * que conserva la talla, cantidad y precio del producto
     */
    @Test
    void aceptaProductoValidoConTalla() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("1", "M", "Playera", 180.0, 2.0)));
        assertEquals(1, r.size());
        assertEquals("M", r.get(0).getTalla());
        assertEquals(2, r.get(0).getCantidad());
        assertEquals(180.0, r.get(0).getPrecio());
    }

    /**
     * Tipo de caso: NORMAL Y LÍMITE
     * Requisitos: RF-10 y RF-12
     *
     * Verifica que el servicio acepte productos que no requieren
     * una talla, como los accesorios, utilizando una cadena vacía
     * para representar la ausencia de esta característica
     *
     * También utiliza una cantidad de una unidad, correspondiente
     * al valor mínimo permitido para una línea del carrito
     *
     * Resultado esperado: el producto es aceptado y su talla
     * permanece como una cadena vacía
     */
    @Test
    void aceptaAccesorioSinTalla() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("8", "", "Llavero", 60.0, 1.0)));
        assertEquals(1, r.size());
        assertEquals("", r.get(0).getTalla());
    }

    /**
     * Tipo de caso: ERROR
     * Requisito: RF-12
     *
     * Verifica que el servicio descarte productos cuya talla
     * no pertenece al conjunto de tallas permitidas
     *
     * Se utiliza la talla XXL, que no forma parte de los
     * valores aceptados por el servicio
     *
     * Resultado esperado: la lista resultante está vacía,
     * debido a que el producto contiene una talla inválida
     */
    @Test
    void descartaTallaInvalida() {
        assertTrue(CarritoService.limpiar(List.of(producto("1", "XXL", "Playera", 180.0, 1.0))).isEmpty());
    }

    /**
     * Tipo de caso: LÍMITE Y ERROR
     * Requisito: RF-12
     *
     * Verifica que el servicio descarte productos que incumplen
     * las reglas de validación de los datos del carrito
     *
     * Se evalúan las siguientes entradas inválidas:
     * - Cantidad igual a cero, inferior al mínimo permitido
     * - Precio negativo
     * - Identificador vacío
     * - Nombre compuesto únicamente por espacios
     *
     * Resultado esperado: todos los productos inválidos son
     * descartados y la lista resultante está vacía
     */
    @Test
    void descartaCantidadMenorAUnoPrecioNegativoIdONombreVacios() {
        List<Map<String, Object>> datos = List.of(
                producto("1", "M", "Playera", 180.0, 0.0),
                producto("2", "M", "Playera", -5.0, 1.0),
                producto("", "M", "Playera", 180.0, 1.0),
                producto("3", "M", "   ", 180.0, 1.0));
        assertTrue(CarritoService.limpiar(datos).isEmpty());
    }

    /**
     * Tipo de caso: LÍMITE
     * Requisito: RF-12
     *
     * Verifica que el servicio respete la cantidad máxima
     * permitida de 99 unidades por línea del carrito
     *
     * Se proporciona una cantidad de 500 unidades para comprobar
     * el comportamiento ante un valor superior al máximo
     *
     * Resultado esperado: la cantidad del producto se ajusta
     * automáticamente a 99 unidades
     */
    @Test
    void limitaCantidadA99() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("1", "M", "Playera", 180.0, 500.0)));
        assertEquals(99, r.get(0).getCantidad());
    }

    /**
     * Tipo de caso: NORMAL Y LÍMITE SUPERIOR
     * Requisitos: RF-10 y RF-12
     *
     * Verifica que dos productos con el mismo identificador
     * y la misma talla se agrupen en una sola línea del carrito
     *
     * Se agregan dos líneas de 60 unidades cada una, cuya suma
     * supera el máximo permitido de 99 unidades
     *
     * Resultado esperado: se genera una única línea del carrito
     * y su cantidad se limita a 99 unidades
     */
    @Test
    void mismoProductoYTallaSeSuman() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(
                producto("1", "M", "Playera", 180.0, 60.0),
                producto("1", "M", "Playera", 180.0, 60.0)));
        assertEquals(1, r.size());
        assertEquals(99, r.get(0).getCantidad());
    }

    /**
     * Tipo de caso: NORMAL
     * Requisitos: RF-10 y RF-12
     *
     * Verifica que dos productos con el mismo identificador,
     * pero con tallas diferentes, se mantengan como líneas
     * independientes dentro del carrito
     *
     * Resultado esperado: se generan dos líneas distintas,
     * conservando las tallas M y L respectivamente
     */
    @Test
    void mismaPrendaConOtraTallaEsOtraLinea() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(
                producto("1", "M", "Playera", 180.0, 1.0),
                producto("1", "L", "Playera", 180.0, 1.0)));
        assertEquals(2, r.size());
        assertEquals("M", r.get(0).getTalla());
        assertEquals("L", r.get(1).getTalla());
    }

    /**
     * Tipo de caso: ERROR
     * Requisito: RF-12
     *
     * Verifica que el servicio ignore elementos que no tienen
     * la estructura esperada de un producto del carrito
     *
     * Se incluyen una cadena de texto, un valor numérico,
     * un valor nulo y un producto válido
     *
     * Resultado esperado: los elementos inválidos son ignorados
     * y únicamente se conserva el producto válido
     */
    @Test
    void ignoraElementosQueNoSonObjetos() {
        List<Object> datos = new ArrayList<>();
        datos.add("texto");
        datos.add(5.0);
        datos.add(null);
        datos.add(producto("1", "", "Taza", 120.0, 1.0));
        assertEquals(1, CarritoService.limpiar(datos).size());
    }

    /**
     * Tipo de caso: LÍMITE
     * Requisito: RF-12
     *
     * Verifica que el servicio limite la longitud de los campos
     * de texto cuando superan el tamaño máximo permitido
     *
     * Los límites evaluados son:
     * - Identificador: 20 caracteres
     * - Nombre: 120 caracteres
     * - Imagen: 255 caracteres
     *
     * Resultado esperado: los campos que exceden estos límites
     * son recortados automáticamente a su longitud máxima
     */
    @Test
    void recortaIdNombreEImagenALosLimites() {
        Map<String, Object> m = producto("1".repeat(30), "", "N".repeat(200), 10.0, 1.0);
        m.put("imagen", "i".repeat(400));
        ItemCarrito item = CarritoService.limpiar(List.of(m)).get(0);
        assertEquals(20, item.getId().length());
        assertEquals(120, item.getNombre().length());
        assertEquals(255, item.getImagen().length());
    }

    /**
     * Tipo de caso: NORMAL
     * Requisito: RF-12
     *
     * Verifica que el servicio convierta correctamente los datos
     * recibidos en diferentes formatos a los tipos requeridos
     * por el modelo ItemCarrito
     *
     * Se proporciona un identificador numérico, un precio
     * representado como texto y una cantidad como texto
     *
     * Resultado esperado: el identificador se convierte a String,
     * el precio a double y la cantidad a int, conservando
     * sus valores correspondientes
     */
    @Test
    void convierteNumerosEscritosComoTextoYEnteroComoId() {
        ItemCarrito item = CarritoService.limpiar(List.of(producto(7.0, "", "Termo", "260.50", "3"))).get(0);
        assertEquals("7", item.getId());
        assertEquals(260.5, item.getPrecio());
        assertEquals(3, item.getCantidad());
    }

    /**
     * Tipo de caso: NORMAL Y ERROR
     * Requisito: RF-12
     *
     * Verifica que el servicio valide los datos del carrito
     * antes de solicitar su almacenamiento mediante el DAO
     *
     * Se utiliza una implementación simulada de CarritoDAO
     * para evitar la dependencia de una base de datos real
     *
     * Se proporciona un producto válido y otro con una talla
     * inválida para comprobar que los datos sean filtrados
     * antes de realizar la operación de reemplazo
     *
     * Resultado esperado: el método reemplazar del DAO se
     * invoca una sola vez y recibe una lista que contiene
     * únicamente un elemento
     *
     * @throws SQLException si ocurre un error de acceso a datos
     *         durante la operación de guardado
     */
    @Test
    void guardarReemplazaElCarritoCompletoConDatosLimpios() throws SQLException {
        // Lista que almacena los datos recibidos por el DAO simulado
        List<List<ItemCarrito>> guardado = new ArrayList<>();
        // Implementación simulada del DAO para evitar utilizar MySQL
        CarritoDAO dao = new CarritoDAO() {
            @Override public void asegurarTabla() { }
            @Override public void reemplazar(int idUsuario, List<ItemCarrito> items) { guardado.add(items); }
        };
        // Se solicita guardar un producto válido y uno inválido
        new CarritoService(dao).guardar(5, List.of(
                producto("1", "S", "Playera", 180.0, 1.0),
                producto("2", "ZZ", "Mala", 1.0, 1.0)));
        // Verifica que el método reemplazar se ejecutó una sola vez
        assertEquals(1, guardado.size());
        // Verifica que únicamente se envió un elemento al DAO
        assertEquals(1, guardado.get(0).size());
    }

    /**
     * Tipo de caso: LÍMITE
     * Requisito: RF-12
     *
     * Verifica que el servicio acepte una cantidad exactamente
     * igual al máximo permitido de 99 unidades
     *
     * Resultado esperado: el producto conserva las 99 unidades
     * sin ser descartado ni modificado
     */
    @Test
    void aceptaCantidadExactamenteIgualA99() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("1", "M", "Playera", 180.0, 99.0)));
        assertEquals(1, r.size());
        assertEquals(99, r.get(0).getCantidad());
    }
    
    /**
     * Tipo de caso: NORMAL.
     * Requisitos: RF-10 y RF-12.
     *
     * Verifica que dos líneas del mismo producto y talla sumen
     * sus cantidades cuando el total no supera el límite de 99.
     *
     * Resultado esperado: queda una sola línea con cantidad 5.
     */
    @Test
    void mismoProductoYTallaSumanSinSuperarLimite() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(
                producto("1", "M", "Playera", 180.0, 2.0),
                producto("1", "M", "Playera", 180.0, 3.0)));
        assertEquals(1, r.size());
        assertEquals(5, r.get(0).getCantidad());
    }

 /**
     * Tipo de caso: LÍMITE.
     * Requisito: RF-12.
     *
     * Verifica que limpiar una lista vacía devuelva una lista
     * vacía sin provocar errores.
     *
     * Resultado esperado: se obtiene una lista vacía.
     */
    @Test
    void limpiarListaVaciaDevuelveListaVacia() {
        assertTrue(CarritoService.limpiar(List.of()).isEmpty());
    }
}
