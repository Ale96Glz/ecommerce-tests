# Monorepo de Pruebas de E-Commerce (ecommerce-tests)

Repositorio centralizado para las pruebas automatizadas del sistema de microservicios de E-Commerce, compuesto por pruebas funcionales de API y pruebas de rendimiento/carga.

## Estructura del Proyecto

```
ecommerce-tests/
├── pom.xml                   # POM Padre (Reactor Maven multi-módulo)
├── README.md                 # Documentacion principal del proyecto
├── .gitignore                # Exclusiones globales de Git
├── api-tests/                # Pruebas funcionales de API (REST Assured, JUnit 5, Allure Reports)
└── load-tests/               # Pruebas de rendimiento y carga (Gatling, Java/Scala DSL)
```

## Requisitos Previos

- Java Development Kit (JDK) version 21 o superior.
- Apache Maven version 3.8 o superior.
- Allure CLI (opcional, para visualizar reportes interactivos).

## Comandos de Ejecucion

Todos los comandos deben ejecutarse desde la raiz del monorepo (`ecommerce-tests`).

### 1. Pruebas Funcionales de API (api-tests)
Ejecutar todas las pruebas de API mediante REST Assured y JUnit 5:
```bash
mvn -pl api-tests test
```

Ejecutar una prueba especifica de API:
```bash
mvn -pl api-tests test "-Dtest=GatewayHealthTest"
```

Generar el reporte estatico con Maven y Allure:
```bash
mvn -pl api-tests allure:report
```

Servir el reporte interactivo en tiempo real con Allure CLI:
```bash
allure serve api-tests/target/allure-results
```

### 2. Pruebas de Carga y Rendimiento (load-tests)
Ejecutar todas las simulaciones de rendimiento con Gatling:
```bash
mvn -pl load-tests gatling:test
```

Ejecutar una simulacion especifica de Gatling (nota el uso de comillas dobles en PowerShell para evitar errores de sintaxis):
```bash
mvn -pl load-tests gatling:test "-Dgatling.simulationClass=sdet.CatalogoProductoSimulation"
```

### 3. Compilacion General del Monorepo
Validar que los dos modulos, codigo y pruebas, compilan sin ejecutar las suites (lo mismo que hace el CI):
```bash
mvn -B test-compile
```

## Nota Importante sobre Rate Limiting

Las pruebas de API y las pruebas de carga no deben ejecutarse simultáneamente contra el mismo gateway si este cuenta con restricciones estrictas de rate-limiting (por ejemplo, un limite de 1 peticion por segundo). Las solicitudes concurrentes masivas de las simulaciones de Gatling saturarán los contadores y provocarán rechazos o fallos en las pruebas funcionales de REST Assured.

## Integracion continua

GitHub Actions compila los dos modulos en cada push y pull request con `mvn -B test-compile`. No ejecuta las suites: los runners no alcanzan el gateway.

## Documentacion

- [Casos de contrato](api-tests/docs/casos.md): un caso por comportamiento del API, con el resultado esperado.
- [Hallazgos de contrato](api-tests/docs/hallazgos.md): defectos de negocio, el contrato actual contra el esperado.
- [Simulaciones de carga](load-tests/docs/pruebas.md): que hace cada escenario, su escala y sus aserciones.
- [Pruebas de contrato](api-tests/README.md) y [pruebas de carga](load-tests/README.md): requisitos y comandos de cada modulo.
