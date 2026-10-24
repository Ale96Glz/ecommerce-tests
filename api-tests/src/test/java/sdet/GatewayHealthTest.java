package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Feature("Gateway")
class GatewayHealthTest extends GatewayTest {
  @Test
  @DisplayName("El gateway responde health")
  @Description("GET /actuator/health responde 200 y status UP.")
  void gatewayRespondeHealth() {
    given()
        .when()
        .get("/actuator/health")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("status", equalTo("UP"));
  }
}
