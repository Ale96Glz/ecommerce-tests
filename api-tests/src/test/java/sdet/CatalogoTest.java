package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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

@Epic("Plataforma E-commerce")
@Feature("Catálogo")
@Severity(SeverityLevel.NORMAL)
class CatalogoTest extends GatewayTest {
  @Test
  @Story("El catálogo de productos es público")
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
  @Story("Un producto por id es público")
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

  @Test
  @Story("El catálogo de categorías es público")
  @DisplayName("El catálogo de categorías es público")
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
  @Story("Un usuario no puede crear un producto")
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

  @Test
  @Story("El catálogo de productos se pagina")
  @DisplayName("El catálogo de productos se pagina")
  @Description("GET /api/v1/producto/pageable responde 200 con content, page 0 y size 1.")
  void catalogoDeProductosSePagina() {
    given()
        .when()
        .get("/api/v1/producto/pageable?page=0&size=1")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("page", equalTo(0))
        .body("size", equalTo(1))
        .body("content.size()", equalTo(1))
        .body("content[0].id", instanceOf(Number.class))
        .body("content[0].nombre", not(emptyOrNullString()));
  }

  @Test
  @Story("Un producto inexistente recibe 404")
  @DisplayName("Un producto inexistente recibe 404")
  @Description("GET /api/v1/producto/{id} de un id ausente responde 404 con el mensaje No se encontró el producto con id.")
  void productoInexistenteRecibe404() {
    noEncontrado(given().when().get("/api/v1/producto/" + AJENO), "No se encontró el producto con id: " + AJENO);
  }

  @Test
  @Story("Crear un producto sin token recibe 401")
  @DisplayName("Crear un producto sin token recibe 401")
  @Description("POST /api/v1/producto sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void crearProductoSinTokenRecibe401() {
    sinBearer(given().contentType(ContentType.JSON).body(productoValido(1)).when().post("/api/v1/producto"));
  }

  @Test
  @Story("Editar un producto sin token recibe 401")
  @DisplayName("Editar un producto sin token recibe 401")
  @Description("PUT /api/v1/producto/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void editarProductoSinTokenRecibe401() {
    sinBearer(given().contentType(ContentType.JSON).body(productoValido(1)).when().put("/api/v1/producto/" + AJENO));
  }

  @Test
  @Story("Borrar un producto sin token recibe 401")
  @DisplayName("Borrar un producto sin token recibe 401")
  @Description("DELETE /api/v1/producto/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void borrarProductoSinTokenRecibe401() {
    sinBearer(given().when().delete("/api/v1/producto/" + AJENO));
  }

  @Test
  @Story("Crear una categoría sin token recibe 401")
  @DisplayName("Crear una categoría sin token recibe 401")
  @Description("POST /api/v1/categoria sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void crearCategoriaSinTokenRecibe401() {
    sinBearer(given().contentType(ContentType.JSON).body(categoriaValida()).when().post("/api/v1/categoria"));
  }

  @Test
  @Story("Editar una categoría sin token recibe 401")
  @DisplayName("Editar una categoría sin token recibe 401")
  @Description("PUT /api/v1/categoria/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void editarCategoriaSinTokenRecibe401() {
    sinBearer(given().contentType(ContentType.JSON).body(categoriaValida()).when().put("/api/v1/categoria/" + AJENO));
  }

  @Test
  @Story("Borrar una categoría sin token recibe 401")
  @DisplayName("Borrar una categoría sin token recibe 401")
  @Description("DELETE /api/v1/categoria/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void borrarCategoriaSinTokenRecibe401() {
    sinBearer(given().when().delete("/api/v1/categoria/" + AJENO));
  }

  @Test
  @Story("Descontar stock sin token recibe 401")
  @DisplayName("Descontar stock sin token recibe 401")
  @Description("PUT /api/v1/producto/{id}/stock sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void descontarStockSinTokenRecibe401() {
    sinBearer(given().when().put("/api/v1/producto/" + AJENO + "/stock?cantidad=1"));
  }

  @Test
  @Story("Reponer stock sin token recibe 401")
  @DisplayName("Reponer stock sin token recibe 401")
  @Description("PUT /api/v1/producto/{id}/stock/reponer sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void reponerStockSinTokenRecibe401() {
    sinBearer(given().when().put("/api/v1/producto/" + AJENO + "/stock/reponer?cantidad=1"));
  }

  @Test
  @Story("Un usuario no puede editar un producto")
  @DisplayName("Un usuario no puede editar un producto")
  @Description("Un USER que edita un producto responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeEditarUnProducto() {
    Number id = primerProducto();
    Number categoriaId = primeraCategoria();
    soloAdmin(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType(ContentType.JSON)
        .body(productoValido(categoriaId))
        .when()
        .put("/api/v1/producto/" + id));
  }

  @Test
  @Story("Un usuario no puede borrar un producto")
  @DisplayName("Un usuario no puede borrar un producto")
  @Description("Un USER que borra un producto responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeBorrarUnProducto() {
    soloAdmin(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .delete("/api/v1/producto/" + primerProducto()));
  }

  @Test
  @Story("Un usuario no puede editar una categoría")
  @DisplayName("Un usuario no puede editar una categoría")
  @Description("Un USER que edita una categoría responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeEditarUnaCategoria() {
    soloAdmin(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType(ContentType.JSON)
        .body(categoriaValida())
        .when()
        .put("/api/v1/categoria/" + primeraCategoria()));
  }

  @Test
  @Story("Un usuario no puede borrar una categoría")
  @DisplayName("Un usuario no puede borrar una categoría")
  @Description("Un USER que borra una categoría responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeBorrarUnaCategoria() {
    soloAdmin(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .delete("/api/v1/categoria/" + primeraCategoria()));
  }

  @Test
  @Story("Descontar stock de un producto inexistente recibe 404")
  @DisplayName("Descontar stock de un producto inexistente recibe 404")
  @Description("PUT /api/v1/producto/{id}/stock de un id ausente responde 404 con el mensaje No se encontró el producto con id.")
  void descontarStockInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().put("/api/v1/producto/" + AJENO + "/stock?cantidad=1"),
        "No se encontró el producto con id: " + AJENO);
  }

  @Test
  @Story("Pedir más stock del disponible recibe 409")
  @DisplayName("Pedir más stock del disponible recibe 409")
  @Description("PUT /api/v1/producto/{id}/stock por encima del stock responde 409 con el mensaje Stock insuficiente.")
  void stockInsuficienteRecibe409() {
    Number id = given().when().get("/api/v1/producto").then().statusCode(200).extract().path("[0].id");
    int disponible = given()
        .when()
        .get("/api/v1/producto/" + id)
        .then()
        .statusCode(200)
        .extract()
        .path("stock");
    int solicitado = disponible + 1;

    given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .put("/api/v1/producto/" + id + "/stock?cantidad=" + solicitado)
        .then()
        .statusCode(409)
        .contentType(containsString("json"))
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo(
            "Stock insuficiente para el producto " + id + ". Disponible: " + disponible + ", solicitado: " + solicitado));
  }

  @Test
  @Story("Reponer stock de un producto inexistente recibe 404")
  @DisplayName("Reponer stock de un producto inexistente recibe 404")
  @Description("PUT /api/v1/producto/{id}/stock/reponer de un id ausente responde 404 con el mensaje No se encontró el producto con id.")
  void reponerStockInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().put("/api/v1/producto/" + AJENO + "/stock/reponer?cantidad=1"),
        "No se encontró el producto con id: " + AJENO);
  }

  private static Map<String, Object> productoValido(Number categoriaId) {
    return Map.of("nombre", "Sdet Prod", "descripcion", "Prueba", "precio", 10.0, "stock", 1, "categoriaId", categoriaId);
  }

  private static Map<String, String> categoriaValida() {
    return Map.of("nombre", "Sdet Cat", "descripcion", "Prueba");
  }

  private static Number primerProducto() {
    return given().when().get("/api/v1/producto").then().statusCode(200).extract().path("[0].id");
  }

  private static Number primeraCategoria() {
    return given().when().get("/api/v1/categoria").then().statusCode(200).extract().path("[0].id");
  }
}
