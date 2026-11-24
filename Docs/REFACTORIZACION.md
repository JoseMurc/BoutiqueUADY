# Guía para refactorizar – Boutique UADY en línea

Proyecto NetBeans: esta carpeta raíz (`pom.xml`, `src/`). Abrir en http://localhost:8080/BoutiqueUady/
(la carpeta `backend-java/` es la variante anterior para Apache+Tomcat; no se usa en NetBeans).

Antes de empezar: importar `boutique_uady.sql` en phpMyAdmin (es seguro repetirlo, no borra datos;
la última versión agrega la tabla `administradores`).

El código **funciona y devuelve las mismas respuestas** que deben conservarse tras refactorizar.
Los siguientes puntos son *code smells* Identificados. Cada uno se puede corregir con la técnica indicada
sin cambiar el comportamiento visible (JSON, comprobante .txt, existencias descontadas).

| # | Archivo / método | Smell | Técnica sugerida |
|---|------------------|-------|------------------|
| 1 | `service/PedidoService.confirmarPedido` | **Long Method** (~110 líneas, 5 pasos mezclados) | Extract Method (`validarSucursal`, `leerCarrito`, `descontarExistencias`, `insertarPedido`, `vaciarCarrito`) |
| 2 | `service/PedidoService` (todo el archivo) | **Capa mezclada**: JDBC (`Connection`, SQL) dentro del servicio | Move Method / Extract Class hacia `PedidoDAO` y `InventarioDAO` |
| 3 | `service/PedidoService` | **Números/cadenas mágicas** (`"pendiente"`, tablas, SQL repetido) | Replace Magic Literal with Constant / enum `EstadoPedido` |
| 4 | `service/CatalogoService.filtrar` | **Condicionales anidados** (hasta 6 niveles) y condición `existencias > 0` repetida 3 veces | Decompose Conditional, Replace Nested Conditional with Guard Clauses, Extract Method `coincide(d, sucursal, talla)` |
| 5 | `service/ComprobanteService.generarTexto` | **Long Parameter List** (7 parámetros) y **código duplicado** (`t = t + ...`, `String.format("%.2f")` x4) | Introduce Parameter Object (usar `Pedido`), StringBuilder, Extract Method `formatearMonto` |
| 6 | `dao/ProductoDAO.listarActivos` y `buscarPorId` | **Código duplicado** (mapeo de ResultSet a `Producto` copiado) | Extract Method `mapear(ResultSet)` |
| 7 | `dao/ProductoDAO.contar` | **Código muerto** (nadie lo llama) y variable `x` / `r` / `p` con **nombres poco claros** | Remove Dead Code, Rename Variable |
| 8 | `controller/CarritoServlet.service` | **Verificación duplicada**: ya la hace `filter/AutenticacionFilter` | Remove Duplicate Code (conservar solo el filtro) |
| 9 | `controller/PedidoServlet`, `ComprobanteServlet`, `CarritoServlet` | **Duplicación**: `((Number) session.getAttribute(USUARIO_ID)).intValue()` | Extract Method en `SesionUtil.idUsuario(request)` |
| 10 | `controller/ProductoServlet.doGet` | Método largo con parseo + respuesta mezclados, códigos HTTP como números (`400`, `404`) | Extract Method, Replace Magic Number with `HttpServletResponse.SC_*` |
| 11 | `dao/AdminDAO` (módulo de administración) | **Large Class**: reportes, inventario, productos y pedidos en una sola clase; devuelve `Map<String,Object>` en vez de modelos (**Primitive Obsession**) | Extract Class (`ReporteDAO`, `InventarioAdminDAO`, `PedidoAdminDAO`), Replace Map with Object |
| 12 | `controller/AdminProductoServlet.doPost` | **Long Method** con parseo de multipart, validación y persistencia mezclados | Extract Method, mover reglas a un `ProductoAdminService` |
| 13 | `dao/AdminDAO.cambiarEstado` | Devuelve `String` de error o `null` como resultado (**Error Code en lugar de excepción**) | Replace Error Code with Exception |

## Reglas del proyecto para no romper nada
- Tras cada refactorización correr `mvn test` (hay 28 pruebas existentes) y probar a mano: catálogo,
  filtros sucursal/talla, agregar al carrito, confirmar pedido, descargar comprobante.
- Salida que debe mantenerse igual: JSON de `/api/productos`, `/api/pedidos`, texto del comprobante,
  y el descuento de inventario (todo o nada).

## Módulo de administración (RF-08 a RF-11)
- Acceso en dos pasos: en el login de la tienda la cuenta llave `admin@uady.com / 12345678` abre `admin/login.html`;
  ahí entran el principal (`A24216404@alumnos.uady.mx / 3431halo`) o los administradores de sucursal.
- `filter/AdminFilter` protege `/api/admin/*` (401 sin sesión de administrador); cada servlet exige su rol (403).
- Principal: `/api/admin/resumen`, `/api/admin/sucursales` (lista, `?id=` detalle, crear/activar), `/api/admin/administradores`.
- Sucursal: `/api/admin/productos` (multipart: crear/editar/baja/alta_existente), `/api/admin/inventario`, `/api/admin/pedidos`.
- Frontend en `admin/*.html` + `assets/js/admin.js` (sesión, menú por rol, alert/confirm) + `assets/js/admin_*.js`.
- Cliente: `mis_pedidos.html` (historial con `GET /api/pedidos`).

## Mapa rápido
- `controller/`: servlets (`/api/login`, `/api/registro`, `/api/sesion`, `/api/productos`, `/api/sucursales`,
  `/api/carrito`, `/api/pedidos`, `/api/comprobante`).
- `filter/AutenticacionFilter`: protege `/api/carrito`, `/api/pedidos`, `/api/comprobante` (401 sin sesión).
- `service/`: reglas de negocio. `dao/`: acceso a MySQL. `model/`: entidades. `exception/`: excepciones propias.
- Datos de ejemplo: 14 productos, sucursales FMAT y Principal; los productos "fmat" existen solo en FMAT.
  Las existencias iniciales (10 FMAT / 6 Principal en ropa, 20 / 12 en accesorios) son valores de ejemplo.
