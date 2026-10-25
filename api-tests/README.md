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

| Caso | Resultado |
|---|---|
| El gateway responde health | 200, `status` UP |
| El registro crea un usuario | 201, `userId` numérico, `email`, `rol` USER, `token` |
| El inicio de sesión devuelve un token | 200, la misma forma que el registro |
| Una contraseña incorrecta recibe 401 | `Credenciales invalidas` |
| Un email inválido recibe 400 | `email: El email no es valido` |
| Una contraseña corta recibe 400 | `password: La contraseña debe tener entre 8 y 72 caracteres` |
| Un email repetido recibe 409 | `Ya existe un usuario con el email: ` más el email |
| Un usuario no puede crear una categoría | 403, `Se requiere rol ADMIN` |
| Un usuario no puede crear un producto | 403, `Se requiere rol ADMIN` |
| El catálogo de productos es público | 200, arreglo con `id` numérico y `nombre` |
| El catálogo de categorías es público | 200, arreglo con `id` numérico y `nombre` |
| Un pedido sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un pago sin token recibe 401 | `Falta el header Authorization Bearer` |
| Una notificación sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un pedido con token inválido recibe 401 | `Token inválido o expirado` |
| Un usuario no puede listar todos los pedidos | 403, `Se requiere rol ADMIN` |
| Un usuario ve sus pedidos | 200, arreglo |

Cada usuario de prueba usa un email `sdet-<millis>-<nano>@ecommerce.test` y la contraseña `Passw0rd!123`.
