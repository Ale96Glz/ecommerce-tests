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

Generar el reporte detallado con Allure:
```bash
mvn -pl api-tests allure:report
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
Validar que la estructura completa compila correctamente sin ejecutar pruebas:
```bash
mvn -q -DskipTests compile
```

## Nota Importante sobre Rate Limiting

Las pruebas de API y las pruebas de carga no deben ejecutarse simultáneamente contra el mismo gateway si este cuenta con restricciones estrictas de rate-limiting (por ejemplo, un limite de 1 peticion por segundo). Las solicitudes concurrentes masivas de las simulaciones de Gatling saturarán los contadores y provocarán rechazos o fallos en las pruebas funcionales de REST Assured.
