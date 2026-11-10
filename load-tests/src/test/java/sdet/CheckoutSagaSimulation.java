package sdet;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.datafaker.Faker;

public class CheckoutSagaSimulation extends Simulation {

  public CheckoutSagaSimulation() {
    String baseUrl = System.getenv("BASE_URL");
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://ecommerce.local";
    }

    int usuarios = Integer.getInteger("load.users", 20);
    int rampaSegundos = Integer.getInteger("load.ramp.seconds", 60);

    HttpProtocolBuilder protocolo = http
        .baseUrl(baseUrl)
        .shareConnections()
        .disableWarmUp();

    ScenarioBuilder saga = scenario("Checkout saga")
        .feed(usuarios())
        .exec(
            http("Registro")
                .post("/api/v1/auth/register")
                .body(StringBody(session ->
                    "{\"nombre\":\"" + json(session.getString("nombre"))
                        + "\",\"email\":\"" + json(session.getString("email"))
                        + "\",\"password\":\"" + json(session.getString("password")) + "\"}"))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(201))
                .check(jsonPath("$.userId").ofLong().exists())
                .check(jsonPath("$.rol").is("USER"))
                .check(jsonPath("$.token").saveAs("token")))
        .exitHereIfFailed()
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Listado")
                .get("/api/v1/producto")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(jsonPath("$[0].id").saveAs("productoId")))
        .exitHereIfFailed()
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Crear pedido")
                .post("/api/v1/pedido")
                .header("Authorization", "Bearer #{token}")
                .body(StringBody("{\"items\":[{\"productoId\":#{productoId},\"cantidad\":1}]}"))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(201))
                .check(jsonPath("$.estado").is("CREADO"))
                .check(jsonPath("$.id").saveAs("pedidoId")))
        .exitHereIfFailed()
        .exec(session -> session.set("estadoPedido", "CREADO"))
        .asLongAsDuring(
            session -> "CREADO".equals(session.getString("estadoPedido")),
            Duration.ofSeconds(20))
        .on(
            pause(Duration.ofSeconds(1))
                .exec(
                    http("Estado del pedido")
                        .get("/api/v1/pedido/#{pedidoId}")
                        .header("Authorization", "Bearer #{token}")
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$.estado").saveAs("estadoPedido"))))
        .exec(
            http("Pedido cerrado")
                .get("/api/v1/pedido/#{pedidoId}")
                .header("Authorization", "Bearer #{token}")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(jsonPath("$.estado").is("PAGADO")))
        .exitHereIfFailed()
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Mis notificaciones")
                .get("/api/v1/notificacion/mias")
                .header("Authorization", "Bearer #{token}")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(jsonPath("$[?(@.tipo == 'PEDIDO_CREADO')].tipo").find().exists())
                .check(jsonPath("$[?(@.tipo == 'PAGO_PROCESADO')].tipo").find().exists()));

    setUp(saga.injectOpen(rampUsers(usuarios).during(Duration.ofSeconds(rampaSegundos))))
        .protocols(protocolo)
        .maxDuration(Duration.ofSeconds(rampaSegundos + 45))
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0));
  }

  private static Iterator<Map<String, Object>> usuarios() {
    Faker faker = new Faker(Locale.forLanguageTag("es"));
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public synchronized Map<String, Object> next() {
        String nombre = faker.name().fullName();
        if (nombre.length() > 100) {
          nombre = nombre.substring(0, 100);
        }
        return Map.of(
            "nombre", nombre,
            "email", "sdet-" + UUID.randomUUID() + "@ecommerce.test",
            "password", faker.internet().password(12, 20, true, true, true));
      }
    };
  }

  private static String json(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
  }
}
