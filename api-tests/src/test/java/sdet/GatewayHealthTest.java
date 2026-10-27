package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@Epic("Plataforma E-commerce")
@Feature("Gateway Health")
@Severity(SeverityLevel.BLOCKER)
class GatewayHealthTest extends GatewayTest {
  @Test
  @Story("El gateway responde health")
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

  @Test
  @Story("El gateway responde liveness")
  @DisplayName("El gateway responde liveness")
  @Description("GET /actuator/health/liveness responde 200 y status UP.")
  void gatewayRespondeLiveness() {
    given()
        .when()
        .get("/actuator/health/liveness")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("status", equalTo("UP"));
  }
}
