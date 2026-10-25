package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

@Feature("Pagos")
class PagosTest extends GatewayTest {
  @Test
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
}
