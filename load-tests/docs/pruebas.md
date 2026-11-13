# Simulaciones de carga

Cada clase en `src/test/java/sdet/` es un escenario: su ruta, su carga y su criterio de éxito. Las cifras son de laboratorio, en la máquina que inyecta, contra el gateway local.

Cómo correrlas está en el [README](../README.md).

## Cómo leer una corrida

El reporte de Gatling agrupa por nombre de petición y muestra mínimo, media, percentiles y máximo. El `index.html` de `target/gatling/` es el gráfico.

**429** en login, registro o una ruta autenticada es el límite de esa ruta, no una caída del gateway. **401** es token ausente o inválido. **5xx** o timeout sí son fallo de capacidad.

Si el CPU al 100 % es el de la máquina que inyecta y el criterio se sigue cumpliendo, el límite es el inyector.

Login y registro: 1 petición por segundo y ráfaga de 5 por IP. El resto autenticado: ráfaga de 20. `GET /actuator/health` no entra en ese límite.

## Rampas cerradas

Modelo cerrado: cada usuario permanece y vuelve a pedir. El ritmo va dentro del bucle. Los peldaños son chicos y el tope llega al final. Cada peldaño dura al menos el ritmo, para que cada usuario alcance a hacer una petición en ese nivel. Las conexiones se comparten.

El quiebre es el primer peldaño en el que falta el criterio o la respuesta pasa de 3 segundos. La corrida sigue hasta el final. El punto se lee en usuarios activos contra latencia y errores.

Valores de la corrida corta:

| Propiedad | Valor | Efecto |
|---|---|---|
| `paceSeconds` | 1 | Segundos entre peticiones de un mismo usuario |
| `stepUsers` | 5 | Usuarios que se suman en cada peldaño |
| `levels` | 4 | Peldaños. El tope es 5, 10, 15 y 20 usuarios |
| `levelSeconds` | 10 | Segundos de cada peldaño. No baja del ritmo |

Las rampas de abajo usan esas propiedades, salvo que se diga otra cosa.

### Health — `sdet.HealthBreakpointSimulation`

`GET /actuator/health`. No usa login ni rutas con límite. Cada respuesta tiene que ser 200 y `status` `UP`, en menos de 3 segundos.

La cifra describe el health. Catálogo, identidad y pedidos se miden en sus escenarios.

### Catálogo — `sdet.CatalogoProductosSimulation`

`GET /api/v1/producto`. Público. No token, no escribe. Hace falta al menos un producto: el criterio mira el primero.

200, `id` numérico y `nombre` con texto, en menos de 3 segundos.

### Catálogo paginado — `sdet.CatalogoProductosPageableSimulation`

`GET /api/v1/producto/pageable?page=0&size=1`. Público. No token, no escribe. Hace falta al menos un producto: el criterio mira el primero de `content`.

200, `page` 0, `size` 1, `id` y `nombre` en `content[0]`, en menos de 3 segundos.

### Detalle — `sdet.CatalogoProductoSimulation`

Antes de la rampa lee el listado y se queda con el `id` del primer producto activo con stock. Esa lectura no entra en la carga. Después solo `GET /api/v1/producto/{id}`. Público. No escribe.

200, ese `id` y `nombre` con texto, en menos de 3 segundos.

### Categorías — `sdet.CatalogoCategoriasSimulation`

`GET /api/v1/categoria`. Público. No token, no escribe. Hace falta al menos una categoría: el criterio mira la primera.

200, `id` numérico y `nombre` con texto, en menos de 3 segundos.

### Perfil — `sdet.PerfilUsuarioSimulation`

Antes de la rampa registra un usuario y se queda con el `token` y el `userId`. No entra en la carga. Después solo `GET /api/v1/usuario/{id}` con Bearer. No edita el perfil.

200, ese `id`, `nombre` con texto, el `email` del registro y rol `USER`, en menos de 3 segundos.

### Existe — `sdet.UsuarioExisteSimulation`

Mismo setup que el perfil. Después solo `GET /api/v1/usuario/existe/{id}` con Bearer. No edita el perfil.

200, ese `id` y rol `USER`, en menos de 3 segundos.

### Mis pedidos — `sdet.PedidosMiosSimulation`

Antes de la rampa registra un usuario y se queda con el `token`. Después solo `GET /api/v1/pedido/mios` con Bearer. No crea pedidos. El listado puede ir vacío: el criterio mira que el cuerpo sea un arreglo JSON.

200 y arreglo, en menos de 3 segundos.

### Mis pagos — `sdet.PagosMiosSimulation`

Igual que mis pedidos, sobre `GET /api/v1/pago/mios`. No crea pagos ni pedidos.

### Mis notificaciones — `sdet.NotificacionesMiasSimulation`

Igual que mis pagos, sobre `GET /api/v1/notificacion/mias`. No crea pedidos ni avisos.

### No leídas — `sdet.NotificacionesNoLeidasSimulation`

Antes de la rampa registra un usuario y se queda con el `token` y el `userId`. Después solo `GET /api/v1/notificacion/usuario/{id}/no-leidas` con Bearer. No crea avisos. El listado puede ir vacío.

200 y arreglo, en menos de 3 segundos.

## Recorridos abiertos

Modelo abierto: cada usuario entra una vez, recorre y sale. Las conexiones se comparten.

El usuario sintético: DataFaker arma el nombre en español (máximo 100 caracteres) y una contraseña de 12 a 20 caracteres con mayúscula, dígito y signo. El correo es `sdet-<uuid>@ecommerce.test`.

Estas corridas **escriben**. Crean usuarios y, si llegan al pedido, descuentan stock. Hay que reponer el producto entre corridas de laboratorio.

### Recorrido completo — `sdet.ProcesoCompletoSimulation`

Cada usuario entra con rampa desde cero hasta `load.users` durante `load.ramp.seconds`. Duración máxima: esa rampa más 45 segundos. Un paso que no cumple el criterio saca al usuario.

Pausa de 2 segundos entre acciones:

1. `POST /api/v1/auth/register` — 201, `userId`, rol `USER`, `token`.
2. `GET /api/v1/producto` — 200. Se guarda el `id` del primero activo, con stock y precio ≤ 100 (el tope de pago).
3. `POST /api/v1/pedido` con una unidad — 201 y estado `CREADO`.
4. `GET /api/v1/pedido/{id}` cada segundo, hasta 20 segundos, mientras siga `CREADO`. Al salir tiene que ser `PAGADO`.
5. `GET /api/v1/notificacion/mias` — 200, con `PEDIDO_CREADO` y `PAGO_PROCESADO`.

Cada petición en menos de 3 segundos. El pago lo cierra el flujo del sistema; la espera cubre ese tiempo.

Sin propiedades: 500 usuarios en 600 segundos. El escenario compra siempre el primer producto pagable (activo, con stock y precio ≤ 100): cuando se acaba el stock, `POST /pedido` responde 409.

Corrida del 5 de noviembre de 2026, gateway local, esos valores: 500 registros y listados bien. 92 pedidos `PAGADO` con las dos notificaciones. 408 con 409. Mediana 12 ms, máxima 251 ms. Reporte: `target/gatling/procesocompletosimulation-20261105064151934/`.

### Saga de checkout — `sdet.CheckoutSagaSimulation`

El mismo recorrido. Tope más bajo y aserciones en el reporte: ninguna petición por encima de 3 segundos y menos del 5 % de fallos. Un 409 al crear el pedido cuenta como fallo (stock, no gateway).

Sin propiedades: 20 usuarios en 60 segundos. `load.users` y `load.ramp.seconds` van en la JVM de Gatling.

### Contención de stock — `sdet.StockConcurrenteSimulation`

No crea productos. Usa el primer producto activo con stock.

Setup (fuera de la carga): `GET /api/v1/producto` y un registro. Se guarda `productoId` y `token`. Un 429 en ese registro sería el límite de identidad.

Carga: `load.users` peticiones `POST /api/v1/pedido` a la vez, **el mismo Bearer** y **el mismo producto**, cantidad 1. En Gatling son muchos usuarios virtuales; en el sistema es **un USER**. El stock se disputa igual: es del producto.

Sin propiedades: 30 pedidos a la vez. La ráfaga autenticada es 20; el resto puede ser 429.

| Código | Significado |
|---|---|
| 201 | Alcanzó una unidad |
| 409 | Stock insuficiente |
| 429 | Límite de la ruta autenticada |
| 5xx | Fallo |

201 y 409 cumplen el criterio de Gatling. Hay que reponer el producto entre corridas.

### Contención de stock entre clientes — `sdet.StockClientesSimulation`

Misma pregunta de inventario, distinto aislamiento: **un comprador por pedido**.

No crea productos. Usa el primer producto activo con stock.

Setup (fuera de la carga): `GET /api/v1/producto` y `load.users` registros, **uno por segundo** (`paceSeconds`, por defecto 1), para no pegarle al límite de identidad (1/s, ráfaga 5). Cada registro 201 deja un `token` propio. Un 429 aquí aborta ese alta: no se dispara un pedido sin cuenta.

Carga: esos mismos N usuarios disparan a la vez `POST /api/v1/pedido`, cada uno con **su** Bearer, una unidad del **mismo** producto.

Sin propiedades: 20 clientes. Coincide con la ráfaga autenticada. Más allá, un 429 en el pedido es el límite de la ruta, no el stock.

Los códigos de `POST /pedido` se leen igual que en `StockConcurrenteSimulation`. Esta corrida crea N usuarios y descuenta stock. Reponer el producto entre corridas.

## Corrida base del 12 de noviembre de 2026

Gateway local, las 15 simulaciones corridas una por una para que un fallo no oculte a las demás. Rampas cerradas con los valores por defecto (pico de 20 usuarios); recorridos abiertos con los suyos, salvo el completo.

| Escenario | Escala | Peticiones | OK | KO | p95 (ms) | p99 (ms) | Máx (ms) |
|---|---|---|---|---|---|---|---|
| Health | 20 usuarios | 585 | 585 | 0 | 7 | 453 | 953 |
| Catálogo productos | 20 usuarios | 585 | 585 | 0 | 9 | 15 | 136 |
| Catálogo paginado | 20 usuarios | 585 | 585 | 0 | 8 | 11 | 178 |
| Detalle | 20 usuarios | 626 | 626 | 0 | 6 | 11 | 136 |
| Categorías | 20 usuarios | 585 | 585 | 0 | 7 | 10 | 130 |
| Perfil | 20 usuarios | 666 | 666 | 0 | 11 | 702 | 1 058 |
| Existe | 20 usuarios | 666 | 666 | 0 | 9 | 23 | 200 |
| Mis pedidos | 20 usuarios | 666 | 666 | 0 | 8 | 12 | 153 |
| Mis pagos | 20 usuarios | 666 | 666 | 0 | 8 | 12 | 171 |
| Mis notificaciones | 20 usuarios | 666 | 666 | 0 | 9 | 18 | 173 |
| No leídas | 20 usuarios | 666 | 666 | 0 | 8 | 12 | 211 |
| Recorrido completo | 10 usuarios / 10 s | 93 | 90 | 3 | 64 | 166 | 166 |
| Saga de checkout | 20 usuarios / 60 s | 251 | 251 | 0 | 62 | 98 | 206 |
| Stock clientes | 20 clientes | 41 | 41 | 0 | 193 | 193 | 193 |
| Stock concurrente | 50 usuarios | 52 | 42 | 10 | 485 | 487 | 487 |

Notas de esa corrida:

- Solo la saga de checkout tiene aserciones. Las otras 14 terminan en `BUILD SUCCESS` aunque tengan KO: ese es el hueco que cierra el paso de aserciones.
- Los 3 KO del recorrido completo son 429 en el listado, residuo del límite de la corrida anterior; 7 de 10 usuarios llegaron al pedido. No es falta de capacidad.
- Los 10 KO de stock concurrente son 429 en `POST /pedido`: 50 a la vez supera la ráfaga autenticada de 20. El check solo acepta 201/409, así que el 429 cuenta como KO. El código usa 50 por defecto y esta guía decía 30: falta decidir la escala.
- Stock al terminar: productos 5 y 7 agotados; 1 con 1, 2 con 19, 3 con 26, 4 con 41 y 6 con 5. Reponer antes de otra corrida que compre.
