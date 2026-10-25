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
@Feature("Notificaciones")
@Severity(SeverityLevel.NORMAL)
class NotificacionesTest extends GatewayTest {
  @Test
  @Story("Una notificación sin token recibe 401")
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

  @Test
  @Story("Un usuario ve sus notificaciones")
  @DisplayName("Un usuario ve sus notificaciones")
  @Description("GET /api/v1/notificacion/mias con token de USER responde 200 con un arreglo.")
  void usuarioVeSusNotificaciones() {
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
        .get("/api/v1/notificacion/mias")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }
}
