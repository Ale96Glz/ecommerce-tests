package sdet;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.datafaker.Faker;

public class NotificacionesNoLeidasSimulation extends Simulation {

  public NotificacionesNoLeidasSimulation() {
    Entorno.Escala escala = Entorno.escala();

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
    AtomicLong usuarioId = new AtomicLong();

    ScenarioBuilder registro = scenario("Registro")
        .exec(
            http("Registro")
                .post("/api/v1/auth/register")
                .body(StringBody(cuerpoRegistro))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(201))
                .check(jsonPath("$.userId").ofLong().saveAs("userId"))
                .check(jsonPath("$.token").saveAs("token")))
        .exec(session -> {
          token.set(session.getString("token"));
          usuarioId.set(session.getLong("userId"));
          return session;
        });

    ScenarioBuilder noLeidas = scenario("No leidas")
        .forever()
        .on(
            pace(Duration.ofSeconds(escala.paceSeconds()))
                .exec(
                    http("GET /api/v1/notificacion/usuario/{id}/no-leidas")
                        .get(session -> "/api/v1/notificacion/usuario/" + usuarioId.get() + "/no-leidas")
                        .header("Authorization", session -> "Bearer " + token.get())
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$").ofList().exists())));

    setUp(
            registro.injectOpen(atOnceUsers(1))
                .andThen(Entorno.escalera(noLeidas, escala)))
        .protocols(Entorno.protocolo())
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(escala.totalSeconds() + 5));
  }

  private static String json(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n");
  }
}
