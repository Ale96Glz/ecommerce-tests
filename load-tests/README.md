# Pruebas de carga del gateway

Este repositorio reúne las simulaciones de Gatling del gateway de laboratorio. Cada clase en `src/test/java/sdet/` es un escenario: su ruta, su carga y su criterio de éxito. `mvn gatling:test` corre todas las simulaciones, una después de otra. Cada una deja su reporte en una carpeta propia.

Hoy están la rampa de health y la del listado público de productos. Las siguientes se agregan en ese paquete, cada una con su clase.

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

## Ejecución

Desde esta carpeta. Corre todas las simulaciones de `src/test/java/sdet/`, una después de otra.

```powershell
$env:BASE_URL = "https://ecommerce.local"
mvn gatling:test
```

Para correr una sola, en PowerShell las comillas van en el `-D`:

```powershell
mvn gatling:test "-Dgatling.simulationClass=sdet.HealthBreakpointSimulation"
```

Para alargar la rampa, las propiedades van en la JVM de Gatling:

```powershell
mvn gatling:test '-Dgatling.jvmArgs=-DstepUsers=10 -Dlevels=6 -DlevelSeconds=20'
```

El reporte queda en `target/gatling/`, en la carpeta de esa corrida. El `index.html` es el gráfico.

## Paquete para el panel

```powershell
mvn gatling:enterprisePackage
```

Genera `target/*-shaded.jar`. Ese JAR incluye todas las simulaciones del repositorio y se sube en Gatling Enterprise, en Packages. En el panel se elige qué clase ejecutar. Hace falta Gatling 3.11 o posterior. El gateway tiene que ser alcanzable desde los inyectores.
