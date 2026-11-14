package sdet;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.incrementConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pace;
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
import java.util.concurrent.atomic.AtomicReference;
import net.datafaker.Faker;

public class NotificacionesMiasSimulation extends Simulation {

  public NotificacionesMiasSimulation() {
    String baseUrl = System.getenv("BASE_URL");
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://ecommerce.local";
    }

    int paceSeconds = Integer.getInteger("paceSeconds", 1);
    int stepUsers = Integer.getInteger("stepUsers", 5);
    int levels = Math.max(Integer.getInteger("levels", 4), 2);
    int levelSeconds = Math.max(Integer.getInteger("levelSeconds", 10), paceSeconds);
    int rampSeconds = 1;
    int totalSeconds = levels * levelSeconds + (levels - 1) * rampSeconds + 2;

    Faker faker = new Faker(Locale.forLanguageTag("es"));
    String nombre = faker.name().fullName();
    if (nombre.length() > 100) {
      nombre = nombre.substring(0, 100);
    }
    String email = "sdet-" + UUID.randomUUID() + "@ecommerce.test";
    String password = faker.credentials().password(12, 20, true, true, true);
    String cuerpoRegistro =
        "{\"nombre\":\"" + json(nombre)
            + "\",\"email\":\"" + json(email)
            + "\",\"password\":\"" + json(password) + "\"}";

    AtomicReference<String> token = new AtomicReference<>("");

    HttpProtocolBuilder protocolo = http
        .baseUrl(baseUrl)
        .shareConnections()
        .disableWarmUp();

    ScenarioBuilder registro = scenario("Registro")
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

    ScenarioBuilder notificaciones = scenario("Mis notificaciones")
        .forever()
        .on(
            pace(Duration.ofSeconds(paceSeconds))
                .exec(
                    http("GET /api/v1/notificacion/mias")
                        .get("/api/v1/notificacion/mias")
                        .header("Authorization", session -> "Bearer " + token.get())
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$").ofList().exists())));

    setUp(
            registro.injectOpen(atOnceUsers(1))
                .andThen(
                    notificaciones.injectClosed(
                        incrementConcurrentUsers(stepUsers)
                            .times(levels)
                            .eachLevelLasting(Duration.ofSeconds(levelSeconds))
                            .separatedByRampsLasting(Duration.ofSeconds(rampSeconds))
                            .startingFrom(stepUsers))))
        .protocols(protocolo)
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(totalSeconds + 5));
  }

  private static String json(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
  }
}
