package sdet;

import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;

@Feature("Identidad")
class IdentidadTest extends GatewayTest {
  @Test
  @DisplayName("El registro crea un usuario")
  void registroCreaUnUsuario() {
    String email = emailNuevo();
    esUsuario(postIdentidad(REGISTRO, usuario(email, CONTRASENA)), 201, email);
  }

  @Test
  @DisplayName("El inicio de sesión devuelve un token")
  void inicioDeSesionDevuelveUnToken() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);

    esUsuario(postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA)), 200, email);
  }

  @Test
  @DisplayName("Una contraseña incorrecta recibe 401")
  void contrasenaIncorrectaRecibe401() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);

    postIdentidad(LOGIN, Map.of("email", email, "password", "clave-mala"))
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Credenciales invalidas"));
  }

  @Test
  @DisplayName("Un usuario no puede crear una categoría")
  void usuarioNoPuedeCrearUnaCategoria() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");

    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Sdet Cat", "descripcion", "Prueba"))
        .when()
        .post("/api/v1/categoria")
        .then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("Se requiere rol ADMIN"));
  }

  @Test
  @DisplayName("Un email inválido recibe 400")
  void emailInvalidoRecibe400() {
    postIdentidad(REGISTRO, usuario("no-es-email", CONTRASENA))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("email: El email no es valido"));
  }

  @Test
  @DisplayName("Un email repetido recibe 409")
  void emailRepetidoRecibe409() {
    String email = emailNuevo();
    Map<String, String> cuerpo = usuario(email, CONTRASENA);
    postIdentidad(REGISTRO, cuerpo).then().statusCode(201);

    postIdentidad(REGISTRO, cuerpo)
        .then()
        .statusCode(409)
        .contentType(containsString("json"))
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo("Ya existe un usuario con el email: " + email));
  }

  private static void esUsuario(Response respuesta, int estado, String email) {
    respuesta.then()
        .statusCode(estado)
        .contentType(containsString("json"))
        .body("email", equalTo(email))
        .body("rol", equalTo("USER"))
        .body("userId", instanceOf(Number.class))
        .body("token.length()", greaterThan(0));
  }
}
