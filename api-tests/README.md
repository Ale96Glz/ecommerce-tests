# Pruebas del gateway

Caja negra contra el gateway de ecommerce, API `1.2.2`. Este módulo es la suite de API. La carga y el estrés van en otro módulo del mismo proyecto, con Gatling.

Las peticiones salen solo por `BASE_URL`. No se llama a los puertos internos de auth, catálogo, pedidos, pagos ni notificaciones.

## Stack

| Pieza | Herramienta | Comando |
|---|---|---|
| API | REST Assured, JUnit 5, Allure | `mvn test` y `mvn allure:serve` |
| Carga y estrés | Gatling | `mvn gatling:test` en su módulo |

REST Assured afirma el código HTTP y la forma del JSON, un comportamiento por test. Gatling repite un recorrido con muchos usuarios y publica peticiones por segundo, errores y percentiles en su propio reporte. Allure no sustituye ese gráfico.

Gatling todavía no está en esta carpeta. Al mover el proyecto, la simulación queda al lado de este módulo, no dentro de `src/test/java`.

## Requisitos

- Java 21 y Maven.
- Gateway de la `1.2.2` levantado.
- `BASE_URL`. Si no se define, las pruebas usan `https://ecommerce.local`.

Login y registro comparten un límite de 1 petición por segundo y una ráfaga de 5 por IP. El resto de las rutas autenticadas permite una ráfaga de 20. `GET /actuator/health` no pasa por ese límite. Un 429 en login se reintenta; en una rampa de Gatling hay que tratarlo como límite, no como caída del gateway. La rampa grande es `GET /actuator/health`.

## API

```powershell
$env:BASE_URL = "https://ecommerce.local"
mvn test
mvn allure:serve
```

El panel se genera en inglés. Abajo en la barra lateral, donde dice EN, se elige Español. Esa elección queda en el navegador.

La severidad sale en `normal` mientras el test no lleve `@Severity`. No se deduce del código HTTP. El Trend es el historial de corridas: cada barra es una ejecución. Para conservar la barra anterior hay que copiar `history` del reporte a `target/allure-results/history` antes de volver a servir.

Al mover la carpeta no hace falta llevar `target/`, `.allure/` ni `allure-results/`. Maven y el plugin de Allure los vuelven a crear.

## Casos

| Grupo | Caso |
|---|---|
| Gateway | El gateway responde health |
| Identidad | El registro crea un usuario |
| Identidad | El inicio de sesión devuelve un token |
| Identidad | Una contraseña incorrecta recibe 401 |
| Identidad | Un usuario no puede crear una categoría |
| Identidad | Un email inválido recibe 400 |
| Identidad | Un email repetido recibe 409 |
| Catálogo | El catálogo de productos es público |
| Catálogo | El catálogo de categorías es público |
| Pedidos | Un pedido sin token recibe 401 |
| Pedidos | Un pedido con token inválido recibe 401 |

## Contrato medido en la 1.2.2

- Registro `201`: `userId` numérico, `email`, `rol` `USER`, `token`. Login `200` con la misma forma.
- Contraseña incorrecta `401`, mensaje `Credenciales invalidas`.
- `GET /api/v1/pedido` sin Bearer `401`, mensaje `Falta el header Authorization Bearer`.
- Bearer inválido `401`, mensaje `Token inválido o expirado`.
- `USER` que crea una categoría `403`, mensaje `Se requiere rol ADMIN`.
- Email inválido `400`, mensaje `email: El email no es valido`.
- Email repetido `409`, mensaje `Ya existe un usuario con el email: ` más el email.
- Health `200`, `status` `UP`.
- `GET /api/v1/producto` y `GET /api/v1/categoria` son públicos y devuelven un arreglo. Cada elemento trae `id` numérico y `nombre`.

Cada test usa un email `sdet-<millis>-<nano>@ecommerce.test`. La contraseña de los usuarios de prueba es `Passw0rd!123`.
