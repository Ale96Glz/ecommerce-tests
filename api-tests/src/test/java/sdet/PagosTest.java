package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

@Epic("Plataforma E-commerce")
@Feature("Pagos")
@Severity(SeverityLevel.CRITICAL)
class PagosTest extends GatewayTest {
  @Test
  @Story("Un pago sin token recibe 401")
  @DisplayName("Un pago sin token recibe 401")
  @Description("GET /api/v1/pago sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pagoSinTokenRecibe401() {
    given()
        .when()
        .get("/api/v1/pago")
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Falta el header Authorization Bearer"));
  }

  @Test
  @Story("Un usuario ve sus pagos")
  @DisplayName("Un usuario ve sus pagos")
  @Description("GET /api/v1/pago/mios con token de USER responde 200 con un arreglo.")
  void usuarioVeSusPagos() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");

    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/api/v1/pago/mios")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede listar todos los pagos")
  @DisplayName("Un usuario no puede listar todos los pagos")
  @Description("Un USER que lista GET /api/v1/pago responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeListarTodosLosPagos() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");

    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/api/v1/pago")
        .then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("Se requiere rol ADMIN"));
  }

  @Test
  @Story("Un usuario no puede paginar los pagos")
  @DisplayName("Un usuario no puede paginar los pagos")
  @Description("Un USER que lista GET /api/v1/pago/pageable responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedePaginarLosPagos() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pago/pageable"));
  }

  @Test
  @Story("Paginar pagos sin token recibe 401")
  @DisplayName("Paginar pagos sin token recibe 401")
  @Description("GET /api/v1/pago/pageable sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void paginarPagosSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/pago/pageable"));
  }

  @Test
  @Story("Los pagos del usuario sin token reciben 401")
  @DisplayName("Los pagos del usuario sin token reciben 401")
  @Description("GET /api/v1/pago/mios sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pagosDelUsuarioSinTokenReciben401() {
    sinBearer(given().when().get("/api/v1/pago/mios"));
  }

  @Test
  @Story("Un pago por id sin token recibe 401")
  @DisplayName("Un pago por id sin token recibe 401")
  @Description("GET /api/v1/pago/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pagoPorIdSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/pago/" + AJENO));
  }

  @Test
  @Story("Un pago inexistente recibe 404")
  @DisplayName("Un pago inexistente recibe 404")
  @Description("GET /api/v1/pago/{id} de un id ausente responde 404 con el mensaje No se encontró el pago con id.")
  void pagoInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pago/" + AJENO),
        "No se encontró el pago con id: " + AJENO);
  }

  @Test
  @Story("Un pedido sin pago recibe 404")
  @DisplayName("Un pedido sin pago recibe 404")
  @Description("GET /api/v1/pago/pedido/{id} de un pedido sin pago responde 404 con el mensaje No se encontró pago para el pedido con id.")
  void pedidoSinPagoRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pago/pedido/" + AJENO),
        "No se encontró pago para el pedido con id: " + AJENO);
  }

  @Test
  @Story("Un usuario ve los pagos de su id")
  @DisplayName("Un usuario ve los pagos de su id")
  @Description("GET /api/v1/pago/usuario/{id} con el token de ese usuario responde 200 con un arreglo.")
  void usuarioVeLosPagosDeSuId() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/pago/usuario/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede ver los pagos de otro")
  @DisplayName("Un usuario no puede ver los pagos de otro")
  @Description("GET /api/v1/pago/usuario/{id} de otro id responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerLosPagosDeOtro() {
    recursoAjeno(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pago/usuario/" + AJENO));
  }

  @Test
  @Story("Crear un pago sin token recibe 401")
  @DisplayName("Crear un pago sin token recibe 401")
  @Description("POST /api/v1/pago sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void crearPagoSinTokenRecibe401() {
    sinBearer(given().contentType("application/json").body(Map.of("pedidoId", 1, "monto", 10.0)).when().post("/api/v1/pago"));
  }

  @Test
  @Story("Un monto cero recibe 400")
  @DisplayName("Un monto cero recibe 400")
  @Description("POST /api/v1/pago con monto 0 responde 400 con el mensaje monto: El monto debe ser mayor a 0.")
  void montoCeroRecibe400() {
    given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType("application/json")
        .body(Map.of("pedidoId", 1, "monto", 0))
        .when()
        .post("/api/v1/pago")
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("monto: El monto debe ser mayor a 0"));
  }

  @Test
  @Story("Un pago sin pedido recibe 400")
  @DisplayName("Un pago sin pedido recibe 400")
  @Description("POST /api/v1/pago sin pedido responde 400 con el mensaje pedidoId: El pedido es obligatorio.")
  void pagoSinPedidoRecibe400() {
    given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType("application/json")
        .body(Map.of("monto", 10.0))
        .when()
        .post("/api/v1/pago")
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("pedidoId: El pedido es obligatorio"));
  }

  @Test
  @Story("Un pago sin monto recibe 400")
  @DisplayName("Un pago sin monto recibe 400")
  @Description("POST /api/v1/pago sin monto responde 400 con el mensaje monto: El monto es obligatorio.")
  void pagoSinMontoRecibe400() {
    given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType("application/json")
        .body(Map.of("pedidoId", 1))
        .when()
        .post("/api/v1/pago")
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("monto: El monto es obligatorio"));
  }

  @Test
  @Story("Los pagos de un usuario sin token reciben 401")
  @DisplayName("Los pagos de un usuario sin token reciben 401")
  @Description("GET /api/v1/pago/usuario/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pagosDeUnUsuarioSinTokenReciben401() {
    sinBearer(given().when().get("/api/v1/pago/usuario/" + AJENO));
  }

  @Test
  @Story("El pago de un pedido sin token recibe 401")
  @DisplayName("El pago de un pedido sin token recibe 401")
  @Description("GET /api/v1/pago/pedido/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pagoDeUnPedidoSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/pago/pedido/" + AJENO));
  }

  @Test
  @Story("Los pagos del usuario con token inválido reciben 401")
  @DisplayName("Los pagos del usuario con token inválido reciben 401")
  @Description("GET /api/v1/pago/mios con un Bearer inválido responde 401 con el mensaje Token inválido o expirado.")
  void pagosDelUsuarioConTokenInvalidoReciben401() {
    bearerInvalido(given().header("Authorization", "Bearer token-falso").when().get("/api/v1/pago/mios"));
  }

  @Test
  @Story("Pagar un pedido inexistente recibe 404")
  @DisplayName("Pagar un pedido inexistente recibe 404")
  @Description("POST /api/v1/pago de un pedido ausente responde 404 con el mensaje No se encontró el pedido con id.")
  void pagarPedidoInexistenteRecibe404() {
    noEncontrado(
        given()
            .header("Authorization", "Bearer " + sesionNueva().token())
            .contentType("application/json")
            .body(Map.of("pedidoId", Long.parseLong(AJENO), "monto", 10.0))
            .when()
            .post("/api/v1/pago"),
        "No se encontró el pedido con id: " + AJENO);
  }

  @Test
  @Story("Un pago por id con token inválido recibe 401")
  @DisplayName("Un pago por id con token inválido recibe 401")
  @Description("GET /api/v1/pago/{id} con un Bearer inválido responde 401 con el mensaje Token inválido o expirado.")
  void pagoPorIdConTokenInvalidoRecibe401() {
    bearerInvalido(given().header("Authorization", "Bearer token-falso").when().get("/api/v1/pago/" + AJENO));
  }

  @Test
  @Story("Pagar el pedido de otro usuario recibe 403")
  @DisplayName("Pagar el pedido de otro usuario recibe 403")
  @Description("POST /api/v1/pago del pedido de otro usuario responde 403 con el mensaje El pedido no pertenece al usuario autenticado.")
  void pagarElPedidoDeOtroUsuarioRecibe403() {
    Sesion dueno = sesionNueva();
    Sesion ajeno = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(dueno, productoId);
      pedidoId = creado.id();
      given()
          .header("Authorization", "Bearer " + ajeno.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", creado.total()))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(403)
          .contentType(containsString("json"))
          .body("status", equalTo(403))
          .body("error", equalTo("Forbidden"))
          .body("message", equalTo("El pedido con id " + pedidoId + " no pertenece al usuario autenticado"));
    } finally {
      devolverPedido(dueno, pedidoId, productoId);
    }
  }

  @Test
  @Story("El sistema paga el pedido automáticamente")
  @DisplayName("El sistema paga el pedido automáticamente")
  @Description("Al crear el pedido, el sistema crea su pago PROCESADO por el total y el pedido pasa a PAGADO.")
  void elSistemaPagaElPedidoAutomaticamente() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      esperarPago(sesion, pedidoId, "PROCESADO")
          .then()
          .contentType(containsString("json"))
          .body("pedidoId", equalTo(pedidoId))
          .body("usuarioId", equalTo(sesion.id()))
          .body("estado", equalTo("PROCESADO"))
          .body("monto", equalTo((float) creado.total().doubleValue()));
      esperarEstado(sesion, pedidoId, "PAGADO", null);
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un monto distinto al del pedido recibe 409")
  @DisplayName("Un monto distinto al del pedido recibe 409")
  @Description("POST /api/v1/pago con un monto distinto al total responde 409 y el pago del sistema queda PROCESADO por el total correcto.")
  void unMontoDistintoAlDelPedidoRecibe409() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      double total = creado.total().doubleValue();
      double distinto = total + 1;
      if (distinto >= 100) {
        distinto = Math.max(total - 1, 0.01);
      }

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", distinto))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(409)
          .contentType(containsString("json"))
          .body("status", equalTo(409))
          .body("error", equalTo("Conflict"))
          .body("message", anyOf(
              containsString("El monto no coincide con el total del pedido"),
              containsString("no está en estado CREADO")));

      esperarPago(sesion, pedidoId, "PROCESADO")
          .then()
          .body("monto", equalTo((float) total));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un monto menor al del pedido recibe 409")
  @DisplayName("Un monto menor al del pedido recibe 409")
  @Description("POST /api/v1/pago con un monto menor al total responde 409 y el pago del sistema queda PROCESADO por el total correcto.")
  void unMontoMenorAlDelPedidoRecibe409() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      double total = creado.total().doubleValue();
      double menor = total - 1;
      if (menor <= 0) {
        menor = 0.01;
      }
      if (menor >= total) {
        throw new IllegalStateException("No hay un monto menor válido para el total " + total);
      }

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", menor))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(409)
          .contentType(containsString("json"))
          .body("status", equalTo(409))
          .body("error", equalTo("Conflict"))
          .body("message", anyOf(
              containsString("El monto no coincide con el total del pedido"),
              containsString("no está en estado CREADO")));

      esperarPago(sesion, pedidoId, "PROCESADO")
          .then()
          .body("monto", equalTo((float) total));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Tras un monto distinto el total exacto queda procesado")
  @DisplayName("Tras un monto distinto el total exacto queda procesado")
  @Description("Tras un 409 por monto distinto, el pago del pedido queda PROCESADO por el total exacto, lo cree el POST o el sistema.")
  void trasUnMontoDistintoElTotalExactoQuedaProcesado() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      double total = creado.total().doubleValue();
      double distinto = total + 1;
      if (distinto >= 100) {
        distinto = Math.max(total - 1, 0.01);
      }

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", distinto))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(409);

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", total))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(anyOf(equalTo(201), equalTo(409)));

      esperarPago(sesion, pedidoId, "PROCESADO")
          .then()
          .contentType(containsString("json"))
          .body("pedidoId", equalTo(pedidoId))
          .body("usuarioId", equalTo(sesion.id()))
          .body("estado", equalTo("PROCESADO"))
          .body("monto", equalTo((float) total));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Pagar un pedido cancelado recibe 409")
  @DisplayName("Pagar un pedido cancelado recibe 409")
  @Description("POST /api/v1/pago de un pedido CANCELADO por el usuario, con el total del pedido, responde 409 y el pedido sigue CANCELADO.")
  void pagarUnPedidoCanceladoRecibe409() throws InterruptedException {
    Sesion sesion = sesionNueva();
    PedidoSobreTope pedido = crearPedidoSobreTope(sesion);
    Number pedidoId = pedido.pedidoId();
    Number total = pedido.total();
    esperarPedidoCanceladoPorPago(sesion, pedidoId);
    esperarStock(pedido.productoId(), pedido.stockAntes());

    // reactivar no publica OrderCreatedEvent: el pedido queda CREADO sin pago automático
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/reactivar")
        .then()
        .statusCode(200)
        .body("estado", equalTo("CREADO"));

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/cancelar")
        .then()
        .statusCode(200)
        .body("estado", equalTo("CANCELADO"));

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(Map.of("pedidoId", pedidoId, "monto", total))
        .when()
        .post("/api/v1/pago")
        .then()
        .statusCode(409)
        .contentType(containsString("json"))
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo(
            "El pedido con id " + pedidoId + " no está en estado CREADO (estado actual: CANCELADO)"));

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/pedido/" + pedidoId)
        .then()
        .statusCode(200)
        .body("estado", equalTo("CANCELADO"))
        .body("motivoCancelacion", equalTo("USUARIO"));
  }

  @Test
  @Story("Pagar de nuevo un pedido pagado recibe 409")
  @DisplayName("Pagar de nuevo un pedido pagado recibe 409")
  @Description("POST /api/v1/pago de un pedido ya pagado responde 409 con el mensaje El pedido ya está pagado.")
  void pagarDeNuevoUnPedidoPagadoRecibe409() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      esperarEstado(sesion, pedidoId, "PAGADO", null);

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", creado.total()))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(409)
          .contentType(containsString("json"))
          .body("status", equalTo(409))
          .body("error", equalTo("Conflict"))
          .body("message", containsString("estado actual: PAGADO"));

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pedido/" + pedidoId)
          .then()
          .statusCode(200)
          .body("estado", equalTo("PAGADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un monto por encima del máximo queda rechazado")
  @DisplayName("Un monto por encima del máximo queda rechazado")
  @Description("El pago automático de un pedido por encima del máximo queda RECHAZADO con el motivo del tope.")
  void montoPorEncimaDelMaximoQuedaRechazado() throws InterruptedException {
    Sesion sesion = sesionNueva();
    PedidoSobreTope pedido = crearPedidoSobreTope(sesion);
    try {
      esperarPago(sesion, pedido.pedidoId(), "RECHAZADO")
          .then()
          .contentType(containsString("json"))
          .body("pedidoId", equalTo(pedido.pedidoId()))
          .body("estado", equalTo("RECHAZADO"))
          .body("motivoRechazo", equalTo("Monto excede el máximo aprobado (100.00)"));
    } finally {
      devolverPedido(sesion, pedido.pedidoId(), pedido.productoId());
    }
  }

  @Test
  @Story("Pagar de nuevo un pedido con pago rechazado recibe 409")
  @DisplayName("Pagar de nuevo un pedido con pago rechazado recibe 409")
  @Description("Tras un pago RECHAZADO, cuando el pedido queda CANCELADO, POST /api/v1/pago con el total responde 409 y el pedido sigue CANCELADO.")
  void pagarDeNuevoUnPedidoConPagoRechazadoRecibe409() throws InterruptedException {
    Sesion sesion = sesionNueva();
    PedidoSobreTope pedido = crearPedidoSobreTope(sesion);
    Number productoId = pedido.productoId();
    Number pedidoId = pedido.pedidoId();
    Number total = pedido.total();
    try {
      esperarPedidoCanceladoPorPago(sesion, pedidoId);

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", total))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(409)
          .contentType(containsString("json"))
          .body("status", equalTo(409))
          .body("error", equalTo("Conflict"))
          .body("message", equalTo(
              "El pedido con id " + pedidoId + " no está en estado CREADO (estado actual: CANCELADO)"));

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pedido/" + pedidoId)
          .then()
          .statusCode(200)
          .body("estado", equalTo("CANCELADO"))
          .body("motivoCancelacion", equalTo("PAGO_RECHAZADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("El pago procesado aparece en los míos")
  @DisplayName("El pago procesado aparece en los míos")
  @Description("GET /api/v1/pago/mios incluye el pago PROCESADO del propio pedido.")
  void pagoProcesadoApareceEnLosMios() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      Number pagoId = esperarPago(sesion, pedidoId, "PROCESADO").path("id");

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pago/mios")
          .then()
          .statusCode(200)
          .contentType(containsString("json"))
          .body("id", hasItem(pagoId));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un usuario ve su pago por id")
  @DisplayName("Un usuario ve su pago por id")
  @Description("GET /api/v1/pago/{id} del propio pago PROCESADO responde 200 con ese id, el pedido y el estado.")
  void usuarioVeSuPagoPorId() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      Number pagoId = esperarPago(sesion, pedidoId, "PROCESADO").path("id");

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pago/" + pagoId)
          .then()
          .statusCode(200)
          .contentType(containsString("json"))
          .body("id", equalTo(pagoId))
          .body("pedidoId", equalTo(pedidoId))
          .body("estado", equalTo("PROCESADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un usuario ve el pago de su pedido")
  @DisplayName("Un usuario ve el pago de su pedido")
  @Description("GET /api/v1/pago/pedido/{id} del propio pedido pagado responde 200 con ese pago y estado PROCESADO.")
  void usuarioVeElPagoDeSuPedido() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoPagable();
    Number pedidoId = null;
    try {
      PedidoCreado creado = crearPedidoPagable(sesion, productoId);
      pedidoId = creado.id();
      Number pagoId = esperarPago(sesion, pedidoId, "PROCESADO").path("id");

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pago/pedido/" + pedidoId)
          .then()
          .statusCode(200)
          .contentType(containsString("json"))
          .body("id", equalTo(pagoId))
          .body("pedidoId", equalTo(pedidoId))
          .body("estado", equalTo("PROCESADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  private static void esperarPedidoCanceladoPorPago(Sesion sesion, Number pedidoId)
      throws InterruptedException {
    for (int intento = 0; intento < 20; intento++) {
      Response respuesta = given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pedido/" + pedidoId);
      if (respuesta.statusCode() == 200
          && "CANCELADO".equals(respuesta.path("estado"))
          && "PAGO_RECHAZADO".equals(respuesta.path("motivoCancelacion"))) {
        return;
      }
      Thread.sleep(1000);
    }
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/pedido/" + pedidoId)
        .then()
        .statusCode(200)
        .body("estado", equalTo("CANCELADO"))
        .body("motivoCancelacion", equalTo("PAGO_RECHAZADO"));
  }

  private static void devolverPedido(Sesion sesion, Number pedidoId, Number productoId) {
    if (pedidoId == null) {
      return;
    }
    Response respuesta = given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/cancelar");
    if (respuesta.statusCode() == 200) {
      return;
    }
    String mensaje = respuesta.path("message");
    if (mensaje != null && mensaje.contains("PAGADO")) {
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .put("/api/v1/producto/" + productoId + "/stock/reponer?cantidad=1")
          .then()
          .statusCode(200);
    }
  }
}
