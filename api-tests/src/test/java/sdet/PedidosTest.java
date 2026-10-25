package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

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
}
