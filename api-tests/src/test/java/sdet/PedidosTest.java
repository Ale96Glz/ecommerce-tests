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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;

@Epic("Plataforma E-commerce")
@Feature("Pedidos")
@Severity(SeverityLevel.CRITICAL)
class PedidosTest extends GatewayTest {
  @Test
  @Story("Un pedido sin token recibe 401")
  @DisplayName("Un pedido sin token recibe 401")
  @Description("GET /api/v1/pedido sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pedidoSinTokenRecibe401() {
    given()
        .when()
        .get("/api/v1/pedido")
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Falta el header Authorization Bearer"));
  }

  @Test
  @Story("Un pedido con token inválido recibe 401")
  @DisplayName("Un pedido con token inválido recibe 401")
  @Description("Un Bearer inválido responde 401 con el mensaje Token inválido o expirado.")
  void pedidoConTokenInvalidoRecibe401() {
    given()
        .header("Authorization", "Bearer token-falso")
        .when()
        .get("/api/v1/pedido")
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Token inválido o expirado"));
  }

  @Test
  @Story("Un usuario no puede listar todos los pedidos")
  @DisplayName("Un usuario no puede listar todos los pedidos")
  @Description("Un USER que lista GET /api/v1/pedido responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeListarTodosLosPedidos() {
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
        .get("/api/v1/pedido")
        .then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("Se requiere rol ADMIN"));
  }

  @Test
  @Story("Un usuario ve sus pedidos")
  @DisplayName("Un usuario ve sus pedidos")
  @Description("GET /api/v1/pedido/mios con token de USER responde 200 con un arreglo.")
  void usuarioVeSusPedidos() {
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
        .get("/api/v1/pedido/mios")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede paginar los pedidos")
  @DisplayName("Un usuario no puede paginar los pedidos")
  @Description("Un USER que lista GET /api/v1/pedido/pageable responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedePaginarLosPedidos() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pedido/pageable"));
  }

  @Test
  @Story("Los pedidos del usuario sin token reciben 401")
  @DisplayName("Los pedidos del usuario sin token reciben 401")
  @Description("GET /api/v1/pedido/mios sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pedidosDelUsuarioSinTokenReciben401() {
    sinBearer(given().when().get("/api/v1/pedido/mios"));
  }

  @Test
  @Story("Paginar pedidos sin token recibe 401")
  @DisplayName("Paginar pedidos sin token recibe 401")
  @Description("GET /api/v1/pedido/pageable sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void paginarPedidosSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/pedido/pageable"));
  }

  @Test
  @Story("Un usuario ve los pedidos de su id")
  @DisplayName("Un usuario ve los pedidos de su id")
  @Description("GET /api/v1/pedido/usuario/{id} con el token de ese usuario responde 200 con un arreglo.")
  void usuarioVeLosPedidosDeSuId() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/pedido/usuario/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede ver los pedidos de otro")
  @DisplayName("Un usuario no puede ver los pedidos de otro")
  @Description("GET /api/v1/pedido/usuario/{id} de otro id responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerLosPedidosDeOtro() {
    recursoAjeno(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pedido/usuario/" + AJENO));
  }

  @Test
  @Story("Un pedido inexistente recibe 404")
  @DisplayName("Un pedido inexistente recibe 404")
  @Description("GET /api/v1/pedido/{id} de un id ausente responde 404 con el mensaje No se encontró el pedido con id.")
  void pedidoInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/pedido/" + AJENO),
        "No se encontró el pedido con id: " + AJENO);
  }

  @Test
  @Story("Un pedido por id sin token recibe 401")
  @DisplayName("Un pedido por id sin token recibe 401")
  @Description("GET /api/v1/pedido/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pedidoPorIdSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/pedido/" + AJENO));
  }

  @Test
  @Story("Crear un pedido sin token recibe 401")
  @DisplayName("Crear un pedido sin token recibe 401")
  @Description("POST /api/v1/pedido sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void crearPedidoSinTokenRecibe401() {
    sinBearer(given().contentType("application/json").body(Map.of("items", java.util.List.of())).when().post("/api/v1/pedido"));
  }

  @Test
  @Story("Cancelar un pedido sin token recibe 401")
  @DisplayName("Cancelar un pedido sin token recibe 401")
  @Description("PUT /api/v1/pedido/{id}/cancelar sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void cancelarPedidoSinTokenRecibe401() {
    sinBearer(given().when().put("/api/v1/pedido/" + AJENO + "/cancelar"));
  }

  @Test
  @Story("Reactivar un pedido sin token recibe 401")
  @DisplayName("Reactivar un pedido sin token recibe 401")
  @Description("PUT /api/v1/pedido/{id}/reactivar sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void reactivarPedidoSinTokenRecibe401() {
    sinBearer(given().when().put("/api/v1/pedido/" + AJENO + "/reactivar"));
  }

  @Test
  @Story("Cancelar un pedido inexistente recibe 404")
  @DisplayName("Cancelar un pedido inexistente recibe 404")
  @Description("PUT /api/v1/pedido/{id}/cancelar de un id ausente responde 404 con el mensaje No se encontró el pedido con id.")
  void cancelarPedidoInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().put("/api/v1/pedido/" + AJENO + "/cancelar"),
        "No se encontró el pedido con id: " + AJENO);
  }

  @Test
  @Story("Reactivar un pedido inexistente recibe 404")
  @DisplayName("Reactivar un pedido inexistente recibe 404")
  @Description("PUT /api/v1/pedido/{id}/reactivar de un id ausente responde 404 con el mensaje No se encontró el pedido con id.")
  void reactivarPedidoInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().put("/api/v1/pedido/" + AJENO + "/reactivar"),
        "No se encontró el pedido con id: " + AJENO);
  }

  @Test
  @Story("Los pedidos de un usuario sin token reciben 401")
  @DisplayName("Los pedidos de un usuario sin token reciben 401")
  @Description("GET /api/v1/pedido/usuario/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void pedidosDeUnUsuarioSinTokenReciben401() {
    sinBearer(given().when().get("/api/v1/pedido/usuario/" + AJENO));
  }

  @Test
  @Story("Los pedidos del usuario con token inválido reciben 401")
  @DisplayName("Los pedidos del usuario con token inválido reciben 401")
  @Description("GET /api/v1/pedido/mios con un Bearer inválido responde 401 con el mensaje Token inválido o expirado.")
  void pedidosDelUsuarioConTokenInvalidoReciben401() {
    bearerInvalido(given().header("Authorization", "Bearer token-falso").when().get("/api/v1/pedido/mios"));
  }

  @Test
  @Story("Un pedido por id con token inválido recibe 401")
  @DisplayName("Un pedido por id con token inválido recibe 401")
  @Description("GET /api/v1/pedido/{id} con un Bearer inválido responde 401 con el mensaje Token inválido o expirado.")
  void pedidoPorIdConTokenInvalidoRecibe401() {
    bearerInvalido(given().header("Authorization", "Bearer token-falso").when().get("/api/v1/pedido/" + AJENO));
  }

  @Test
  @Story("Crear un pedido descuenta el stock")
  @DisplayName("Crear un pedido descuenta el stock")
  @Description("POST /api/v1/pedido responde 201 con estado CREADO y el stock del producto baja una unidad.")
  void crearUnPedidoDescuentaElStock() {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    int antes = stockDe(productoId);
    Number pedidoId = null;
    try {
      pedidoId = given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("items", List.of(Map.of("productoId", productoId, "cantidad", 1))))
          .when()
          .post("/api/v1/pedido")
          .then()
          .statusCode(201)
          .contentType(containsString("json"))
          .body("id", instanceOf(Number.class))
          .body("usuarioId", equalTo(sesion.id()))
          .body("estado", equalTo("CREADO"))
          .body("items[0].cantidad", equalTo(1))
          .extract()
          .path("id");

      given()
          .when()
          .get("/api/v1/producto/" + productoId)
          .then()
          .statusCode(200)
          .body("stock", equalTo(antes - 1));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Pedir un producto inexistente recibe 404")
  @DisplayName("Pedir un producto inexistente recibe 404")
  @Description("POST /api/v1/pedido de un producto ausente responde 404 con el mensaje No existe el producto con id.")
  void pedirUnProductoInexistenteRecibe404() {
    noEncontrado(
        given()
            .header("Authorization", "Bearer " + sesionNueva().token())
            .contentType("application/json")
            .body(Map.of("items", List.of(Map.of("productoId", Long.parseLong(AJENO), "cantidad", 1))))
            .when()
            .post("/api/v1/pedido"),
        "No existe el producto con id: " + AJENO);
  }

  @Test
  @Story("Un usuario ve su pedido por id")
  @DisplayName("Un usuario ve su pedido por id")
  @Description("GET /api/v1/pedido/{id} con el token del dueño responde 200 con estado CREADO.")
  void usuarioVeSuPedidoPorId() {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pedido/" + pedidoId)
          .then()
          .statusCode(200)
          .contentType(containsString("json"))
          .body("id", equalTo(pedidoId))
          .body("usuarioId", equalTo(sesion.id()))
          .body("estado", equalTo("CREADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un usuario no puede ver un pedido ajeno")
  @DisplayName("Un usuario no puede ver un pedido ajeno")
  @Description("GET /api/v1/pedido/{id} de otro usuario responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerUnPedidoAjeno() {
    Sesion dueno = sesionNueva();
    Sesion ajeno = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(dueno, productoId);
      recursoAjeno(given()
          .header("Authorization", "Bearer " + ajeno.token())
          .when()
          .get("/api/v1/pedido/" + pedidoId));
    } finally {
      devolverPedido(dueno, pedidoId, productoId);
    }
  }

  @Test
  @Story("Un usuario no puede cancelar un pedido ajeno")
  @DisplayName("Un usuario no puede cancelar un pedido ajeno")
  @Description("PUT /api/v1/pedido/{id}/cancelar de otro usuario responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeCancelarUnPedidoAjeno() {
    Sesion dueno = sesionNueva();
    Sesion ajeno = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(dueno, productoId);
      recursoAjeno(given()
          .header("Authorization", "Bearer " + ajeno.token())
          .when()
          .put("/api/v1/pedido/" + pedidoId + "/cancelar"));
    } finally {
      devolverPedido(dueno, pedidoId, productoId);
    }
  }

  @Test
  @Story("Cancelar un pedido creado repone el stock")
  @DisplayName("Cancelar un pedido creado repone el stock")
  @Description("PUT /api/v1/pedido/{id}/cancelar responde 200 con estado CANCELADO y el stock vuelve a la unidad descontada.")
  void cancelarUnPedidoCreadoReponeElStock() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    int antes = stockDe(productoId);
    Number pedidoId = crearPedido(sesion, productoId);

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/cancelar")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(pedidoId))
        .body("estado", equalTo("CANCELADO"))
        .body("motivoCancelacion", equalTo("USUARIO"));

    int stock = antes - 1;
    for (int intento = 0; intento < 20 && stock != antes; intento++) {
      Thread.sleep(1000);
      stock = stockDe(productoId);
    }
    given()
        .when()
        .get("/api/v1/producto/" + productoId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(antes));
  }

  @Test
  @Story("El pedido creado aparece en los míos")
  @DisplayName("El pedido creado aparece en los míos")
  @Description("GET /api/v1/pedido/mios incluye el pedido recién creado.")
  void pedidoCreadoApareceEnLosMios() {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pedido/mios")
          .then()
          .statusCode(200)
          .contentType(containsString("json"))
          .body("id", hasItem(pedidoId));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Reactivar un pedido cancelado por el usuario recibe 409")
  @DisplayName("Reactivar un pedido cancelado por el usuario recibe 409")
  @Description("PUT /api/v1/pedido/{id}/reactivar de un pedido cancelado por el usuario responde 409 con el motivo USUARIO.")
  void reactivarPedidoCanceladoPorElUsuarioRecibe409() {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = crearPedido(sesion, productoId);
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/cancelar")
        .then()
        .statusCode(200);

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/reactivar")
        .then()
        .statusCode(409)
        .contentType(containsString("json"))
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo(
            "No se puede reactivar el pedido " + pedidoId
                + " porque está en estado CANCELADO (motivo de cancelación: USUARIO)"));
  }

  @Test
  @Story("El pedido pagado pasa a PAGADO")
  @DisplayName("El pedido pagado pasa a PAGADO")
  @Description("Tras un pago PROCESADO, GET /api/v1/pedido/{id} responde 200 con estado PAGADO.")
  void pedidoPagadoPasaAPagado() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", 10.0))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(201);

      esperarEstado(sesion, pedidoId, "PAGADO", null);
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("Cancelar un pedido pagado recibe 409")
  @DisplayName("Cancelar un pedido pagado recibe 409")
  @Description("PUT /api/v1/pedido/{id}/cancelar de un pedido PAGADO responde 409 porque está en estado PAGADO.")
  void cancelarUnPedidoPagadoRecibe409() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", 10.0))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(201);
      esperarEstado(sesion, pedidoId, "PAGADO", null);

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .put("/api/v1/pedido/" + pedidoId + "/cancelar")
          .then()
          .statusCode(409)
          .contentType(containsString("json"))
          .body("status", equalTo(409))
          .body("error", equalTo("Conflict"))
          .body("message", equalTo(
              "No se puede cancelar el pedido " + pedidoId + " porque está en estado PAGADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("El pedido con pago rechazado queda cancelado")
  @DisplayName("El pedido con pago rechazado queda cancelado")
  @Description("Tras un pago RECHAZADO, GET /api/v1/pedido/{id} responde 200 con estado CANCELADO, motivo PAGO_RECHAZADO y el stock vuelve.")
  void pedidoConPagoRechazadoQuedaCancelado() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    int antes = stockDe(productoId);
    Number pedidoId = crearPedido(sesion, productoId);
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(Map.of("pedidoId", pedidoId, "monto", 1000.0))
        .when()
        .post("/api/v1/pago")
        .then()
        .statusCode(201);

    esperarEstado(sesion, pedidoId, "CANCELADO", "PAGO_RECHAZADO");
    esperarStock(productoId, antes);
  }

  @Test
  @Story("Reactivar un pedido con pago rechazado lo deja creado")
  @DisplayName("Reactivar un pedido con pago rechazado lo deja creado")
  @Description("PUT /api/v1/pedido/{id}/reactivar de un pedido cancelado por pago rechazado responde 200 con estado CREADO.")
  void reactivarPedidoConPagoRechazadoLoDejaCreado() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", 1000.0))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(201);
      esperarEstado(sesion, pedidoId, "CANCELADO", "PAGO_RECHAZADO");

      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .put("/api/v1/pedido/" + pedidoId + "/reactivar")
          .then()
          .statusCode(200)
          .contentType(containsString("json"))
          .body("id", equalTo(pedidoId))
          .body("estado", equalTo("CREADO"));
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  private static void esperarEstado(Sesion sesion, Number pedidoId, String estado, String motivo)
      throws InterruptedException {
    for (int intento = 0; intento < 20; intento++) {
      Response respuesta = given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .get("/api/v1/pedido/" + pedidoId);
      if (respuesta.statusCode() == 200
          && estado.equals(respuesta.path("estado"))
          && (motivo == null || motivo.equals(respuesta.path("motivoCancelacion")))) {
        break;
      }
      Thread.sleep(1000);
    }
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/pedido/" + pedidoId)
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("estado", equalTo(estado))
        .body("motivoCancelacion", motivo == null ? nullValue() : equalTo(motivo));
  }

  private static void esperarStock(Number productoId, int esperado) throws InterruptedException {
    for (int intento = 0; intento < 20 && stockDe(productoId) != esperado; intento++) {
      Thread.sleep(1000);
    }
    given()
        .when()
        .get("/api/v1/producto/" + productoId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(esperado));
  }

  private static Number productoConStock() {
    List<Map<String, Object>> productos = given()
        .when()
        .get("/api/v1/producto")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("");
    for (Map<String, Object> producto : productos) {
      int stock = ((Number) producto.get("stock")).intValue();
      if (stock >= 1 && !"AGOTADO".equals(String.valueOf(producto.get("estado")))) {
        return (Number) producto.get("id");
      }
    }
    throw new IllegalStateException("No hay un producto con stock");
  }

  private static int stockDe(Number productoId) {
    return given()
        .when()
        .get("/api/v1/producto/" + productoId)
        .then()
        .statusCode(200)
        .extract()
        .path("stock");
  }

  private static Number crearPedido(Sesion sesion, Number productoId) {
    return given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(Map.of("items", List.of(Map.of("productoId", productoId, "cantidad", 1))))
        .when()
        .post("/api/v1/pedido")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
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
