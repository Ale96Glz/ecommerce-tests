# Pruebas de carga del gateway

Simulaciones Gatling contra el gateway de laboratorio. Cada clase en `src/test/java/sdet/` es un escenario. Qué hace cada una, el criterio y cómo leer 429 o 409 está en [docs/pruebas.md](docs/pruebas.md).

Las cifras son de la máquina que inyecta, contra `BASE_URL`. Las pruebas de contrato están en el módulo `api-tests`.

## Requisitos

- Java 21 y Maven.
- Gateway levantado.
- `BASE_URL`. Si no se define: `https://ecommerce.local`.

No correr carga y API a la vez contra el mismo gateway: el rate limit las pisa. Login y registro: 1/s, ráfaga 5. Rutas autenticadas: ráfaga 20. Health no entra en el límite. Un 429 es el límite, no una caída.

`mvn gatling:test` corre **todas** las simulaciones, una detrás de otra. El recorrido completo, la saga y las de stock **escriben** y descuentan inventario.

## Ejecución

Desde la raíz del monorepo, o desde esta carpeta.

```powershell
$env:BASE_URL = "https://ecommerce.local"
mvn -pl load-tests gatling:test
```

Una sola (en PowerShell las comillas van en el `-D`):

```powershell
mvn -pl load-tests gatling:test "-Dgatling.simulationClass=sdet.CatalogoProductoSimulation"
```

Rampa cerrada más larga:

```powershell
mvn -pl load-tests gatling:test "-Dgatling.simulationClass=sdet.CatalogoProductosSimulation" "-Dgatling.jvmArgs=-DstepUsers=10 -Dlevels=6 -DlevelSeconds=20"
```

Recorrido completo (sin esto: 500 usuarios en 600 s):

```powershell
mvn -pl load-tests gatling:test "-Dgatling.simulationClass=sdet.ProcesoCompletoSimulation" "-Dgatling.jvmArgs=-Dload.users=500 -Dload.ramp.seconds=600"
```

El reporte queda en `target/gatling/`, carpeta de esa corrida. El `index.html` es el gráfico.

## Paquete para el panel

```powershell
mvn -pl load-tests gatling:enterprisePackage
```

Genera `target/*-shaded.jar` (Gatling 3.11 o posterior). Se sube en Packages; en el panel se elige la clase. El gateway tiene que ser alcanzable desde los inyectores.
