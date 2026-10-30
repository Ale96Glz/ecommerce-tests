# Casos de prueba

Cada fila es un test. El nombre es el `@DisplayName` y el resultado es el contrato: código HTTP y forma del JSON.

Cada usuario de prueba usa un email `sdet-<millis>-<nano>@ecommerce.test` y la contraseña `Passw0rd!123`.

## Gateway

| Caso | Resultado |
|---|---|
| El gateway responde health | 200, `status` UP |
| El gateway responde liveness | 200, `status` UP |
| El gateway responde readiness | 200, `status` UP |

## Identidad

| Caso | Resultado |
|---|---|
| El registro crea un usuario | 201, `userId` numérico, `email`, `rol` USER, `token` |
| El inicio de sesión devuelve un token | 200, la misma forma que el registro |
| Una contraseña incorrecta recibe 401 | `Credenciales invalidas` |
| Un email inválido recibe 400 | `email: El email no es valido` |
| Un registro con email vacío recibe 400 | `email: El email no puede estar vacio` |
| Una contraseña corta recibe 400 | `password: La contraseña debe tener entre 8 y 72 caracteres` |
| Un email repetido recibe 409 | `Ya existe un usuario con el email: ` más el email |
| Un usuario no puede crear una categoría | 403, `Se requiere rol ADMIN` |
| Un nombre corto recibe 400 | `nombre: El nombre debe tener entre 2 y 100 caracteres` |
| Una contraseña larga recibe 400 | `password: La contraseña debe tener entre 8 y 72 caracteres` |
| Un login con email inválido recibe 400 | `email: El email no es valido` |
| Un login con contraseña vacía recibe 400 | `password: La contraseña no puede estar vacia` |
| Un login con email vacío recibe 400 | `email: El email no puede estar vacio` |
| Un email desconocido recibe 401 | `Credenciales invalidas` |
| Listar usuarios sin token recibe 401 | `Falta el header Authorization Bearer` |
| Ver un perfil sin token recibe 401 | `Falta el header Authorization Bearer` |
| Comprobar existencia sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario no puede listar los usuarios | 403, `Se requiere rol ADMIN` |
| Un usuario no puede paginar los usuarios | 403, `Se requiere rol ADMIN` |
| Un usuario ve su perfil | 200, id, nombre, email y rol USER |
| Un usuario no puede ver otro perfil | 403, `No puedes acceder a recursos de otro usuario` |
| Un usuario cambia su nombre | 200, el nombre nuevo |
| Un usuario no puede editar otro perfil | 403, `No puedes acceder a recursos de otro usuario` |
| Editar el perfil con un nombre corto recibe 400 | `nombre: El nombre debe tener entre 2 y 100 caracteres` |
| Editar el perfil con un email inválido recibe 400 | `email: El email no es valido` |
| Editar el perfil con un email repetido recibe 409 | `Ya existe un usuario con el email: ` más el email |
| Un usuario cambia su email | 200, el email nuevo |
| Editar el perfil sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario comprueba que existe | 200, id y rol USER |
| Un usuario inexistente recibe 404 | `No existe un usuario válido con id` |
| Un usuario no puede cambiar un rol | 403, `Se requiere rol ADMIN` |
| Un usuario no puede borrar un usuario | 403, `Se requiere rol ADMIN` |

## Catálogo

| Caso | Resultado |
|---|---|
| Un usuario no puede crear un producto | 403, `Se requiere rol ADMIN` |
| El catálogo de productos es público | 200, arreglo con `id` numérico y `nombre` |
| Un producto por id es público | 200, `id` numérico y `nombre` |
| El catálogo de categorías es público | 200, arreglo con `id` numérico y `nombre` |
| El catálogo de productos se pagina | 200, `content`, page 0 y size 1 |
| Un producto inexistente recibe 404 | `No se encontró el producto con id` |
| Crear un producto sin token recibe 401 | `Falta el header Authorization Bearer` |
| Editar un producto sin token recibe 401 | `Falta el header Authorization Bearer` |
| Borrar un producto sin token recibe 401 | `Falta el header Authorization Bearer` |
| Crear una categoría sin token recibe 401 | `Falta el header Authorization Bearer` |
| Editar una categoría sin token recibe 401 | `Falta el header Authorization Bearer` |
| Borrar una categoría sin token recibe 401 | `Falta el header Authorization Bearer` |
| Descontar stock sin token recibe 401 | `Falta el header Authorization Bearer` |
| Reponer stock sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario no puede editar un producto | 403, `Se requiere rol ADMIN` |
| Un usuario no puede borrar un producto | 403, `Se requiere rol ADMIN` |
| Un usuario no puede editar una categoría | 403, `Se requiere rol ADMIN` |
| Un usuario no puede borrar una categoría | 403, `Se requiere rol ADMIN` |
| Descontar stock de un producto inexistente recibe 404 | `No se encontró el producto con id` |
| Pedir más stock del disponible recibe 409 | `Stock insuficiente para el producto` |
| Reponer stock de un producto inexistente recibe 404 | `No se encontró el producto con id` |

## Pedidos

| Caso | Resultado |
|---|---|
| Un pedido sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un pedido con token inválido recibe 401 | `Token inválido o expirado` |
| Un usuario no puede listar todos los pedidos | 403, `Se requiere rol ADMIN` |
| Un usuario ve sus pedidos | 200, arreglo |
| Un usuario no puede paginar los pedidos | 403, `Se requiere rol ADMIN` |
| Los pedidos del usuario sin token reciben 401 | `Falta el header Authorization Bearer` |
| Paginar pedidos sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario ve los pedidos de su id | 200, arreglo |
| Un usuario no puede ver los pedidos de otro | 403, `No puedes acceder a recursos de otro usuario` |
| Un pedido inexistente recibe 404 | `No se encontró el pedido con id` |
| Un pedido por id sin token recibe 401 | `Falta el header Authorization Bearer` |
| Crear un pedido sin token recibe 401 | `Falta el header Authorization Bearer` |
| Cancelar un pedido sin token recibe 401 | `Falta el header Authorization Bearer` |
| Reactivar un pedido sin token recibe 401 | `Falta el header Authorization Bearer` |
| Cancelar un pedido inexistente recibe 404 | `No se encontró el pedido con id` |
| Reactivar un pedido inexistente recibe 404 | `No se encontró el pedido con id` |
| Los pedidos de un usuario sin token reciben 401 | `Falta el header Authorization Bearer` |
| Los pedidos del usuario con token inválido reciben 401 | `Token inválido o expirado` |
| Un pedido por id con token inválido recibe 401 | `Token inválido o expirado` |
| Crear un pedido descuenta el stock | 201, estado `CREADO` y el stock baja una unidad |
| Un usuario ve su pedido por id | 200, estado `CREADO` |
| Un usuario no puede ver un pedido ajeno | 403, `No puedes acceder a recursos de otro usuario` |
| Cancelar un pedido creado repone el stock | 200, estado `CANCELADO` y el stock vuelve |
| El pedido creado aparece en los míos | 200, el id está en el arreglo |
| Reactivar un pedido cancelado por el usuario recibe 409 | motivo de cancelación `USUARIO` |
| El pedido pagado pasa a PAGADO | 200, estado `PAGADO` |
| El pedido con pago rechazado queda cancelado | 200, estado `CANCELADO`, motivo `PAGO_RECHAZADO` y el stock vuelve |
| Reactivar un pedido con pago rechazado lo deja creado | 200, estado `CREADO` |

## Pagos

| Caso | Resultado |
|---|---|
| Un pago sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario ve sus pagos | 200, arreglo |
| Un usuario no puede listar todos los pagos | 403, `Se requiere rol ADMIN` |
| Un usuario no puede paginar los pagos | 403, `Se requiere rol ADMIN` |
| Paginar pagos sin token recibe 401 | `Falta el header Authorization Bearer` |
| Los pagos del usuario sin token reciben 401 | `Falta el header Authorization Bearer` |
| Un pago por id sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un pago inexistente recibe 404 | `No se encontró el pago con id` |
| Un pedido sin pago recibe 404 | `No se encontró pago para el pedido con id` |
| Un usuario ve los pagos de su id | 200, arreglo |
| Un usuario no puede ver los pagos de otro | 403, `No puedes acceder a recursos de otro usuario` |
| Crear un pago sin token recibe 401 | `Falta el header Authorization Bearer` |
| Los pagos de un usuario sin token reciben 401 | `Falta el header Authorization Bearer` |
| El pago de un pedido sin token recibe 401 | `Falta el header Authorization Bearer` |
| Los pagos del usuario con token inválido reciben 401 | `Token inválido o expirado` |
| Pagar un pedido inexistente recibe 404 | `No se encontró el pedido con id` |
| Un pago por id con token inválido recibe 401 | `Token inválido o expirado` |
| Pagar el pedido de otro usuario recibe 403 | `El pedido con id` no pertenece al usuario autenticado |
| Un usuario paga su pedido | 201, estado `PROCESADO` |
| El pago procesado aparece en los míos | 200, el id está en el arreglo |
| Pagar de nuevo un pedido pagado recibe 409 | `El pedido con id` ya está pagado |
| Un monto por encima del máximo queda rechazado | 201, estado `RECHAZADO` y el tope aprobado |

## Notificaciones

| Caso | Resultado |
|---|---|
| Una notificación sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario ve sus notificaciones | 200, arreglo |
| Un usuario no puede listar todas las notificaciones | 403, `Se requiere rol ADMIN` |
| Un usuario no puede paginar las notificaciones | 403, `Se requiere rol ADMIN` |
| Listar notificaciones sin token recibe 401 | `Falta el header Authorization Bearer` |
| Paginar notificaciones sin token recibe 401 | `Falta el header Authorization Bearer` |
| Una notificación inexistente recibe 404 | `No se encontró la notificación con id` |
| Una notificación por id sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un usuario ve las notificaciones de su id | 200, arreglo |
| Un usuario no puede ver las notificaciones de otro | 403, `No puedes acceder a recursos de otro usuario` |
| Un usuario ve sus notificaciones no leídas | 200, arreglo |
| Un usuario no puede ver las no leídas de otro | 403, `No puedes acceder a recursos de otro usuario` |
| Marcar una notificación inexistente recibe 404 | `No se encontró la notificación con id` |
| Marcar una notificación sin token recibe 401 | `Falta el header Authorization Bearer` |
| Crear una notificación sin token recibe 401 | `Falta el header Authorization Bearer` |
| Un tipo de notificación inválido recibe 400 | `Tipo de notificación inválido` |
| Un usuario crea su notificación | 201, tipo, mensaje, referencia y `leida` falso |
| Un usuario marca su notificación como leída | 200, `leida` verdadero |
| Un usuario crea un aviso de pago procesado | 201, tipo `PAGO_PROCESADO` y `leida` falso |
| Un usuario vuelve a marcar su notificación leída | 200, `leida` sigue verdadero |
| Notificaciones propias con token inválido reciben 401 | `Token inválido o expirado` |
| Un usuario ve su notificación por id | 200, tipo y `leida` falso |
| Un usuario no puede ver la notificación de otro | 403, `No puedes acceder a recursos de otro usuario` |
| Un usuario no puede marcar la notificación de otro | 403 y sigue sin leerse |
| La notificación creada aparece en no leídas | 200, el id está en el arreglo |
| La notificación leída sale de no leídas | 200, el id ya no está |
| El pedido creado avisa al usuario | aviso `PEDIDO_CREADO` de ese pedido |
| El pago procesado avisa al usuario | aviso `PAGO_PROCESADO` con estado `PROCESADO` |
| El pago rechazado avisa al usuario | aviso `PAGO_PROCESADO` con estado `RECHAZADO` y el tope |
