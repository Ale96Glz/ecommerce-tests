package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@Feature("Catálogo")
class CatalogoTest extends GatewayTest {
  @Test
  @DisplayName("El catálogo de productos es público")
  @Description("GET /api/v1/producto responde 200 con un arreglo de id numérico y nombre.")
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
  @DisplayName("Un producto por id es público")
  @Description("GET /api/v1/producto/{id} sin Authorization responde 200 con id numérico y nombre.")
  void productoPorIdEsPublico() {
    Number id = given()
        .when()
        .get("/api/v1/producto")
        .then()
        .statusCode(200)
        .extract()
        .path("[0].id");

    given()
        .when()
        .get("/api/v1/producto/" + id)
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", instanceOf(Number.class))
        .body("id", equalTo(id))
        .body("nombre", not(emptyOrNullString()));
  }
  @Description("GET /api/v1/categoria responde 200 con un arreglo de id numérico y nombre.")
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

  @Test
  @DisplayName("Un usuario no puede crear un producto")
  @Description("Un USER que crea un producto responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeCrearUnProducto() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");
    Number categoriaId = given()
        .when()
        .get("/api/v1/categoria")
        .then()
        .statusCode(200)
        .extract()
        .path("[0].id");

    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nombre", "Sdet Prod",
            "descripcion", "Prueba",
            "precio", 10.0,
            "stock", 1,
            "categoriaId", categoriaId))
        .when()
        .post("/api/v1/producto")
        .then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("Se requiere rol ADMIN"));
  }
}
