package sdet;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

abstract class GatewayTest {
  static final String REGISTRO = "/api/v1/auth/register";
  static final String LOGIN = "/api/v1/auth/login";
  static final String CONTRASENA = "Passw0rd!123";
  static final String AJENO = "9000000000000";

  private static boolean listo;

  @BeforeAll
  static void preparar() {
    if (listo) return;
    String base = System.getenv("BASE_URL");
    RestAssured.baseURI = base == null || base.isBlank() ? "https://ecommerce.local" : base;
    RestAssured.useRelaxedHTTPSValidation();
    RestAssured.filters(
        new ReintentoLimite(),
        new AllureRestAssured()
            .setRequestAttachmentName("Petición HTTP")
            .setResponseAttachmentName("Respuesta HTTP"));
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    listo = true;
  }

  @Step("Crear un email de prueba")
  static String emailNuevo() {
    return "sdet-" + System.currentTimeMillis() + "-" + Long.toHexString(System.nanoTime()) + "@ecommerce.test";
  }

  @Step("Armar el cuerpo del usuario")
  static Map<String, String> usuario(String email, String password) {
    return Map.of("nombre", "Prueba Sdet", "email", email, "password", password);
  }

  @Step("Enviar la petición de identidad")
  static Response postIdentidad(String ruta, Map<String, String> cuerpo) {
    Response respuesta = null;
    for (int intento = 0; intento < 4; intento++) {
      respuesta = RestAssured.given().contentType(ContentType.JSON).body(cuerpo).when().post(ruta);
      if (respuesta.statusCode() != 429) return respuesta;
      try {
        Thread.sleep(1100);
      } catch (InterruptedException error) {
        Thread.currentThread().interrupt();
        return respuesta;
      }
    }
    return respuesta;
  }

  @Step("Abrir una sesión de usuario")
  static Sesion sesionNueva() {
    String email = emailNuevo();
    Response registro = postIdentidad(REGISTRO, usuario(email, CONTRASENA));
    registro.then().statusCode(201);
    Number id = registro.path("userId");
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");
    return new Sesion(id, email, token);
  }

  @Step("Crear un pedido cuyo total supera el tope de pago")
  static PedidoSobreTope crearPedidoSobreTope(Sesion sesion) {
    Map<String, Object> producto = productoParaSuperarTope();
    Number productoId = (Number) producto.get("id");
    int cantidad = ((Number) producto.get("cantidad")).intValue();
    int stockAntes = ((Number) RestAssured.given()
        .when()
        .get("/api/v1/producto/" + productoId)
        .then()
        .statusCode(200)
        .extract()
        .path("stock")).intValue();
    Response respuesta = RestAssured.given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType(ContentType.JSON)
        .body(Map.of("items", List.of(Map.of("productoId", productoId, "cantidad", cantidad))))
        .when()
        .post("/api/v1/pedido");
    respuesta.then().statusCode(201);
    return new PedidoSobreTope(productoId, respuesta.path("id"), respuesta.path("total"), stockAntes);
  }

  @Step("Elegir un producto cuyo total pueda superar el tope de pago")
  static Map<String, Object> productoParaSuperarTope() {
    List<Map<String, Object>> productos = RestAssured.given()
        .when()
        .get("/api/v1/producto")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("");
    for (Map<String, Object> producto : productos) {
      int stock = ((Number) producto.get("stock")).intValue();
      double precio = ((Number) producto.get("precio")).doubleValue();
      if (stock < 1 || precio <= 0 || "AGOTADO".equals(String.valueOf(producto.get("estado")))) {
        continue;
      }
      int cantidad = precio > 100 ? 1 : (int) Math.floor(100.0 / precio) + 1;
      if (stock >= cantidad) {
        return Map.of("id", producto.get("id"), "cantidad", cantidad);
      }
    }
    throw new IllegalStateException("No hay un producto cuyo total pueda superar el tope de 100");
  }

  @Step("Comprobar el 401 por falta de Bearer")
  static void sinBearer(Response respuesta) {
    respuesta.then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Falta el header Authorization Bearer"));
  }

  @Step("Comprobar el 401 de un Bearer inválido")
  static void bearerInvalido(Response respuesta) {
    respuesta.then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Token inválido o expirado"));
  }

  @Step("Comprobar el 403 de ADMIN")
  static void soloAdmin(Response respuesta) {
    respuesta.then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("Se requiere rol ADMIN"));
  }

  @Step("Comprobar el 403 de otro usuario")
  static void recursoAjeno(Response respuesta) {
    respuesta.then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("No puedes acceder a recursos de otro usuario"));
  }

  @Step("Comprobar un 404")
  static void noEncontrado(Response respuesta, String mensaje) {
    respuesta.then()
        .statusCode(404)
        .contentType(containsString("json"))
        .body("status", equalTo(404))
        .body("error", equalTo("Not Found"))
        .body("message", equalTo(mensaje));
  }

  record Sesion(Number id, String email, String token) {}

  record PedidoSobreTope(Number productoId, Number pedidoId, Number total, int stockAntes) {}

  private static final class ReintentoLimite implements Filter {
    private static final ThreadLocal<Boolean> activo = ThreadLocal.withInitial(() -> false);

    @Override
    public Response filter(
        FilterableRequestSpecification peticion,
        FilterableResponseSpecification respuestaEsperada,
        FilterContext contexto) {
      if (activo.get()) {
        return contexto.next(peticion, respuestaEsperada);
      }
      activo.set(true);
      try {
        Response respuesta = contexto.next(peticion, respuestaEsperada);
        for (int intento = 0; intento < 8 && respuesta.statusCode() == 429; intento++) {
          try {
            Thread.sleep(1100);
          } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return respuesta;
          }
          respuesta = RestAssured.given().spec(peticion).request(peticion.getMethod(), peticion.getURI());
        }
        return respuesta;
      } finally {
        activo.set(false);
      }
    }
  }
}
