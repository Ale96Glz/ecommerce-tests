# Pruebas de API y de rendimiento

Contrato HTTP de identidad, catálogo, pedidos, pagos y notificaciones. Cada test comprueba un comportamiento: el código HTTP y la forma del JSON. Las peticiones salen por `BASE_URL`. No se llama a los puertos internos de los servicios.

La carga va con Gatling, en un módulo hermano. Todavía no está en esta carpeta.

## Requisitos

- Java 21 y Maven.
- API de la `1.2.2` levantada.
- `BASE_URL`. Si no se define, las pruebas usan `https://ecommerce.local`.

Login y registro permiten 1 petición por segundo y una ráfaga de 5 por IP. El resto de las rutas autenticadas permite una ráfaga de 20. `GET /actuator/health` no entra en ese límite. Un 429 es el límite, no una caída. En login se reintenta.

## Ejecución

`BASE_URL` fija el gateway. Si no se define, las pruebas usan `https://ecommerce.local`.

La suite completa:

```powershell
$env:BASE_URL = "https://ecommerce.local"
mvn test
```

Una clase:

```powershell
mvn "-Dtest=IdentidadTest" test
```

Un método. El nombre es el del método Java, no el `@DisplayName`:

```powershell
mvn "-Dtest=IdentidadTest#loginConEmailVacioRecibe400" test
```

Varios métodos de la misma clase, unidos con `+`:

```powershell
mvn "-Dtest=IdentidadTest#loginConEmailVacioRecibe400+registroConEmailVacioRecibe400" test
```

Métodos de clases distintas, separados con una coma:

```powershell
mvn "-Dtest=PedidosTest#crearUnPedidoDescuentaElStock,PagosTest#pagarElPedidoDeOtroUsuarioRecibe403" test
```

En PowerShell las comillas van en `-Dtest`. Sin ellas, `#` se toma como comentario y el filtro se corta.

`mvn test` genera el reporte en `target/allure-report` y guarda la barra de esa ejecución. La Tendencia muestra una barra más a partir de la segunda corrida. Para abrirlo:

```powershell
mvn allure:serve
```

## Casos

Los casos y su resultado están en [docs/casos.md](docs/casos.md).
