package sdet;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@Feature("Catálogo")
class CatalogoTest extends GatewayTest {
  @Test
  @DisplayName("El catálogo de productos es público")
  void catalogoDeProductosEsPublico() {
    given()
        .when()
        .get("/api/v1/producto")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("size()", greaterThan(0))
        .body("[0].id", instanceOf(Number.class))
        .body("[0].nombre", not(emptyOrNullString()));
  }

  @Test
  @DisplayName("El catálogo de categorías es público")
  void catalogoDeCategoriasEsPublico() {
    given()
        .when()
        .get("/api/v1/categoria")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("size()", greaterThan(0))
        .body("[0].id", instanceOf(Number.class))
        .body("[0].nombre", not(emptyOrNullString()));
  }
}
