package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Feature("Notificaciones")
class NotificacionesTest extends GatewayTest {
  @Test
  @DisplayName("Una notificación sin token recibe 401")
  @Description("GET /api/v1/notificacion/mias sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void notificacionSinTokenRecibe401() {
    given()
        .when()
        .get("/api/v1/notificacion/mias")
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Falta el header Authorization Bearer"));
  }
}
