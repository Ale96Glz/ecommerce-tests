# Pruebas de carga del gateway

Este repositorio reúne las simulaciones de Gatling del gateway de laboratorio. Cada clase en `src/test/java/sdet/` es un escenario: su ruta, su carga y su criterio de éxito. `mvn gatling:test` corre todas las simulaciones, una después de otra. Cada una deja su reporte en una carpeta propia.

Hoy están la rampa de health, la del listado público de productos, la del listado paginado, la del detalle de un producto, la del listado público de categorías, el recorrido completo de un usuario y las rampas autenticadas del perfil, mis pedidos, mis pagos y mis notificaciones. Las siguientes se agregan en ese paquete, cada una con su clase. El recorrido completo registra usuarios y crea pedidos: cada corrida descuenta stock. Las rampas del perfil, mis pedidos, mis pagos y mis notificaciones registran un usuario para el token y no escriben.

La cifra de cada corrida es de laboratorio, en la máquina que inyecta, contra el gateway local.

Las pruebas de contrato del api están en [ecommerce-rest-assured](https://github.com/Ale96Glz/ecommerce-rest-assured)

## Requisitos

- Java 21 y Maven.
- Gateway levantado.
- `BASE_URL`. Si no se define, la simulación usa `https://ecommerce.local`. Esa dirección solo la ve la máquina local.

## Escenarios

| Simulación | Escenario |
|---|---|
| `sdet.HealthBreakpointSimulation` | Rampa de `GET /actuator/health` |
| `sdet.CatalogoProductosSimulation` | Rampa de `GET /api/v1/producto` |
| `sdet.CatalogoProductosPageableSimulation` | Rampa de `GET /api/v1/producto/pageable` |
| `sdet.CatalogoProductoSimulation` | Rampa de `GET /api/v1/producto/{id}` |
| `sdet.CatalogoCategoriasSimulation` | Rampa de `GET /api/v1/categoria` |
| `sdet.ProcesoCompletoSimulation` | Recorrido de registro a pedido pagado |
| `sdet.PerfilUsuarioSimulation` | Rampa de `GET /api/v1/usuario/{id}` |
| `sdet.PedidosMiosSimulation` | Rampa de `GET /api/v1/pedido/mios` |
| `sdet.PagosMiosSimulation` | Rampa de `GET /api/v1/pago/mios` |
| `sdet.NotificacionesMiasSimulation` | Rampa de `GET /api/v1/notificacion/mias` |

El reporte de Gatling agrupa por nombre de petición y muestra mínimo, media, percentiles y máximo.

## Rampa de health

La simulación es `sdet.HealthBreakpointSimulation`.

`GET /actuator/health` no entra en el límite de peticiones. Login, registro y el resto de rutas autenticadas sí tienen límite: un 429 ahí es el límite de esa ruta, no una caída del gateway. Este escenario no las usa. La cifra de esta rampa describe el health; login, catálogo y pedidos se miden en sus propios escenarios.

Modelo cerrado: cada usuario permanece y vuelve a pedir el health. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo, para que cada usuario alcance a hacer una petición en ese nivel. Las conexiones se comparten, así no se abre un socket por usuario.

Cada respuesta tiene que ser 200 y `status` `UP`, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, `status` deja de ser `UP`, o la respuesta pasa ese tiempo. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el health sigue `UP`, el límite es el inyector.

Valores de la corrida corta:

| Propiedad | Valor | Efecto |
|---|---|---|
| `paceSeconds` | 1 | Segundos entre peticiones de un mismo usuario |
| `stepUsers` | 5 | Usuarios que se suman en cada peldaño |
| `levels` | 4 | Peldaños. El tope es 5, 10, 15 y 20 usuarios |
| `levelSeconds` | 10 | Segundos de cada peldaño. No baja del ritmo |

## Rampa del catálogo

La simulación es `sdet.CatalogoProductosSimulation`.

Pide el listado público de productos. No envía token y no escribe. El catálogo tiene que tener al menos un producto: el criterio mira el primero.

Modelo cerrado, el mismo de health: cada usuario permanece y vuelve a pedir el listado. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200, con `id` numérico y `nombre` con texto, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, el primer producto no trae ese JSON, o la respuesta pasa ese tiempo. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el listado sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa del catálogo paginado

La simulación es `sdet.CatalogoProductosPageableSimulation`.

Pide una página pública de productos, `page=0` y `size=1`. No envía token y no escribe. El catálogo tiene que tener al menos un producto: el criterio mira el primero de `content`.

Modelo cerrado, el mismo del listado: cada usuario permanece y vuelve a pedir esa página. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200, con `page` 0, `size` 1, `id` numérico y `nombre` con texto en el primer ítem de `content`, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, la página no trae ese JSON, o la respuesta pasa ese tiempo. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y la página sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa del detalle

La simulación es `sdet.CatalogoProductoSimulation`.

Antes de la rampa lee `GET /api/v1/producto` y se queda con el `id` del primer producto. Esa lectura no entra en la carga. Después pide solo `GET /api/v1/producto/{id}`. No envía token y no escribe. El catálogo tiene que tener al menos un producto.

Modelo cerrado, el mismo del listado: cada usuario permanece y vuelve a pedir ese detalle. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200, con ese `id` y `nombre` con texto, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, el detalle no trae ese JSON, o la respuesta pasa ese tiempo. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el detalle sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa de categorías

La simulación es `sdet.CatalogoCategoriasSimulation`.

Pide el listado público de categorías. No envía token y no escribe. El catálogo tiene que tener al menos una categoría: el criterio mira la primera.

Modelo cerrado, el mismo del listado de productos: cada usuario permanece y vuelve a pedir el listado. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200, con `id` numérico y `nombre` con texto, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, la primera categoría no trae ese JSON, o la respuesta pasa ese tiempo. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el listado sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa del perfil

La simulación es `sdet.PerfilUsuarioSimulation`.

Antes de la rampa registra un usuario y se queda con el `token` y el `userId`. Esa lectura no entra en la carga. Después pide solo `GET /api/v1/usuario/{id}` con ese Bearer. No edita el perfil.

Modelo cerrado, el mismo de mis pedidos: cada usuario permanece y vuelve a pedir ese perfil. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200, con ese `id`, `nombre` con texto, el `email` del registro y rol `USER`, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, el perfil no trae ese JSON, o la respuesta pasa ese tiempo. Un 401 es token ausente o inválido. Un 429 es el límite de esa ruta, no una caída del gateway. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el perfil sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa de mis pedidos

La simulación es `sdet.PedidosMiosSimulation`.

Antes de la rampa registra un usuario y se queda con el `token`. Esa lectura no entra en la carga. Después pide solo `GET /api/v1/pedido/mios` con ese Bearer. No crea pedidos. El listado puede ir vacío: el criterio mira que la respuesta sea un arreglo JSON, no que haya un pedido.

Modelo cerrado, el mismo del catálogo: cada usuario permanece y vuelve a pedir ese listado. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200 y un arreglo JSON, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, el cuerpo no es un arreglo, o la respuesta pasa ese tiempo. Un 401 es token ausente o inválido. Un 429 es el límite de esa ruta, no una caída del gateway. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el listado sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa de mis pagos

La simulación es `sdet.PagosMiosSimulation`.

Antes de la rampa registra un usuario y se queda con el `token`. Esa lectura no entra en la carga. Después pide solo `GET /api/v1/pago/mios` con ese Bearer. No crea pagos ni pedidos. El listado puede ir vacío: el criterio mira que la respuesta sea un arreglo JSON, no que haya un pago.

Modelo cerrado, el mismo de mis pedidos: cada usuario permanece y vuelve a pedir ese listado. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200 y un arreglo JSON, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, el cuerpo no es un arreglo, o la respuesta pasa ese tiempo. Un 401 es token ausente o inválido. Un 429 es el límite de esa ruta, no una caída del gateway. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el listado sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Rampa de mis notificaciones

La simulación es `sdet.NotificacionesMiasSimulation`.

Antes de la rampa registra un usuario y se queda con el `token`. Esa lectura no entra en la carga. Después pide solo `GET /api/v1/notificacion/mias` con ese Bearer. No crea pedidos ni notificaciones. El listado puede ir vacío: el criterio mira que la respuesta sea un arreglo JSON, no que haya una notificación.

Modelo cerrado, el mismo de mis pagos: cada usuario permanece y vuelve a pedir ese listado. El ritmo va dentro del bucle y la duración máxima corta la corrida. Los peldaños son chicos y el tope de usuarios llega al final. Cada peldaño dura al menos el ritmo. Las conexiones se comparten.

Cada respuesta tiene que ser 200 y un arreglo JSON, en menos de 3 segundos. El quiebre es el primer peldaño en el que falta el 200, el cuerpo no es un arreglo, o la respuesta pasa ese tiempo. Un 401 es token ausente o inválido. Un 429 es el límite de esa ruta, no una caída del gateway. La corrida sigue hasta el final. El punto se lee en el gráfico de usuarios activos contra latencia y errores.

Si el CPU al 100 % es el de la máquina que inyecta y el listado sigue cumpliendo el criterio, el límite es el inyector.

La corrida corta usa las mismas propiedades y los mismos valores que la rampa de health.

## Recorrido completo

La simulación es `sdet.ProcesoCompletoSimulation`.

Modelo abierto. Cada usuario entra una vez, recorre el proceso y sale. La entrada sube en línea recta desde cero hasta `load.users` durante `load.ramp.seconds`. La duración máxima es esa rampa más 45 segundos, para que quien entra al final alcance a terminar. Las conexiones se comparten.

El usuario es sintético. DataFaker arma el nombre en español, recortado a 100 caracteres, y una contraseña de 12 a 20 caracteres con mayúscula, dígito y signo. El correo es `sdet-<uuid>@ecommerce.test`. El token del registro queda en la sesión de ese usuario y viaja como `Bearer` en el pedido, la consulta y las notificaciones. Un paso que no cumple el criterio saca al usuario del escenario.

El recorrido, con una pausa de 2 segundos entre acciones:

1. `POST /api/v1/auth/register`. La respuesta tiene que ser 201, con `userId`, rol `USER` y `token`.
2. `GET /api/v1/producto`. La respuesta tiene que ser 200. Se guarda el `id` del primer producto.
3. `POST /api/v1/pedido` con una unidad de ese producto. La respuesta tiene que ser 201 y el estado `CREADO`.
4. `GET /api/v1/pedido/{id}` cada segundo, hasta 20 segundos, mientras el estado siga en `CREADO`. Al salir de esa espera, el estado tiene que ser `PAGADO`.
5. `GET /api/v1/notificacion/mias`. La respuesta tiene que ser 200 e incluir `PEDIDO_CREADO` y `PAGO_PROCESADO`.

Cada petición tiene que responder en menos de 3 segundos. El pedido pasa a `PAGADO` por el flujo del sistema, después de que pagos procesa el evento. La espera cubre ese tiempo.

Valores de la corrida:

| Propiedad | Valor | Efecto |
|---|---|---|
| `load.users` | 500 | Usuarios que entran durante la rampa |
| `load.ramp.seconds` | 600 | Segundos en los que entran, de menos a más |

Corrida de laboratorio del 5 de noviembre de 2026, con esos valores, contra el gateway local. Entraron 500 usuarios. El ritmo al final de la rampa fue de unos 1,7 por segundo y hubo como mucho unos 12 activos a la vez. Registro y listado respondieron bien las 500 veces. 92 pedidos llegaron a `PAGADO` y vieron las dos notificaciones. 408 recibieron 409 al crear el pedido: el escenario siempre compra el primer producto del listado y ese stock se agotó. La mediana de respuesta fue 12 ms y la máxima 251 ms. El reporte quedó en `target/gatling/procesocompletosimulation-20261105064151934/`.

## Ejecución

Desde esta carpeta. Corre todas las simulaciones de `src/test/java/sdet/`, una después de otra.

```powershell
$env:BASE_URL = "https://ecommerce.local"
mvn gatling:test
```

Para correr una sola, en PowerShell las comillas van en el `-D`:

```powershell
mvn gatling:test "-Dgatling.simulationClass=sdet.CatalogoProductoSimulation"
```

Para alargar una rampa cerrada, las propiedades van en la JVM de Gatling:

```powershell
mvn gatling:test "-Dgatling.simulationClass=sdet.CatalogoProductosSimulation" "-Dgatling.jvmArgs=-DstepUsers=10 -Dlevels=6 -DlevelSeconds=20"
```

Para el recorrido completo, los usuarios y la duración de la rampa también van en esa JVM. Sin esas propiedades usa 500 usuarios en 600 segundos:

```powershell
mvn gatling:test "-Dgatling.simulationClass=sdet.ProcesoCompletoSimulation" "-Dgatling.jvmArgs=-Dload.users=500 -Dload.ramp.seconds=600"
```

El reporte queda en `target/gatling/`, en la carpeta de esa corrida. El `index.html` es el gráfico.

## Paquete para el panel

```powershell
mvn gatling:enterprisePackage
```

Genera `target/*-shaded.jar`. Ese JAR incluye todas las simulaciones del repositorio y se sube en Gatling Enterprise, en Packages. En el panel se elige qué clase ejecutar. Hace falta Gatling 3.11 o posterior. El gateway tiene que ser alcanzable desde los inyectores.
