package sdet;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.datafaker.Faker;

public class StockConcurrenteSimulation extends Simulation {

  public StockConcurrenteSimulation() {
    String baseUrl = System.getenv("BASE_URL");
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://ecommerce.local";
    }

    int usuarios = Integer.getInteger("load.users", 20);

    Faker faker = new Faker(Locale.forLanguageTag("es"));
    String nombre = faker.name().fullName();
    if (nombre.length() > 100) {
      nombre = nombre.substring(0, 100);
    }
    String email = "sdet-" + UUID.randomUUID() + "@ecommerce.test";
    String password = faker.internet().password(12, 20, true, true, true);
    String cuerpoRegistro =
        "{\"nombre\":\"" + json(nombre)
            + "\",\"email\":\"" + json(email)
            + "\",\"password\":\"" + json(password) + "\"}";

    AtomicReference<String> token = new AtomicReference<>("");
    AtomicLong productoId = new AtomicLong();

    HttpProtocolBuilder protocolo = http
        .baseUrl(baseUrl)
        .shareConnections()
        .disableWarmUp();

    ScenarioBuilder preparacion = scenario("Preparacion")
        .exec(
            http("GET /api/v1/producto")
                .get("/api/v1/producto")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(Productos.conStock()))
        .exec(session -> {
          productoId.set(session.getLong("productoId"));
          return session;
        })
        .exec(
            http("Registro")
                .post("/api/v1/auth/register")
                .body(StringBody(cuerpoRegistro))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(201))
                .check(jsonPath("$.token").saveAs("token")))
        .exec(session -> {
          token.set(session.getString("token"));
          return session;
        });

    ScenarioBuilder compra = scenario("Stock concurrente")
        .exec(
            http("POST /api/v1/pedido")
                .post("/api/v1/pedido")
                .header("Authorization", session -> "Bearer " + token.get())
                .body(StringBody(session ->
                    "{\"items\":[{\"productoId\":" + productoId.get() + ",\"cantidad\":1}]}"))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().in(201, 409)));

    setUp(
            preparacion.injectOpen(atOnceUsers(1))
                .andThen(compra.injectOpen(atOnceUsers(usuarios))))
        .protocols(protocolo)
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(15));
  }

  private static String json(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
  }
}
