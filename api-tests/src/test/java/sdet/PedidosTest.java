package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Feature("Pedidos")
class PedidosTest extends GatewayTest {
  @Test
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
}
