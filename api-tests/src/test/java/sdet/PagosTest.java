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
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(dueno, productoId);
      given()
          .header("Authorization", "Bearer " + ajeno.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", 10.0))
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
