package sdet;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

abstract class GatewayTest {
  static final String REGISTRO = "/api/v1/auth/register";
  static final String LOGIN = "/api/v1/auth/login";
  static final String CONTRASENA = "Passw0rd!123";

  private static boolean listo;

  @BeforeAll
  static void preparar() {
    if (listo) return;
    String base = System.getenv("BASE_URL");
    RestAssured.baseURI = base == null || base.isBlank() ? "https://ecommerce.local" : base;
    RestAssured.useRelaxedHTTPSValidation();
    RestAssured.filters(new AllureRestAssured());
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    listo = true;
  }

  static String emailNuevo() {
    return "sdet-" + System.currentTimeMillis() + "-" + Long.toHexString(System.nanoTime()) + "@ecommerce.test";
  }

  static Map<String, String> usuario(String email, String password) {
    return Map.of("nombre", "Prueba Sdet", "email", email, "password", password);
  }

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
}
