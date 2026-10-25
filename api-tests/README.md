# Pruebas de API y de rendimiento

Contrato HTTP de identidad, catálogo, pedidos, pagos y notificaciones. Cada test comprueba un comportamiento: el código HTTP y la forma del JSON. Las peticiones salen por `BASE_URL`. No se llama a los puertos internos de los servicios.

La carga va con Gatling, en un módulo hermano. Todavía no está en esta carpeta.

## Requisitos

- Java 21 y Maven.
- API de la `1.2.2` levantada.
- `BASE_URL`. Si no se define, las pruebas usan `https://ecommerce.local`.

Login y registro permiten 1 petición por segundo y una ráfaga de 5 por IP. El resto de las rutas autenticadas permite una ráfaga de 20. `GET /actuator/health` no entra en ese límite. Un 429 es el límite, no una caída. En login se reintenta.

## Ejecución

```powershell
$env:BASE_URL = "https://ecommerce.local"
mvn test
mvn allure:serve
```

`mvn test` guarda la barra de esa ejecución. La Tendencia muestra una barra más a partir de la segunda corrida.

## Casos

Los casos y su resultado están en [docs/casos.md](docs/casos.md).
