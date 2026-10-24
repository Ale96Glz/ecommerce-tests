package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
