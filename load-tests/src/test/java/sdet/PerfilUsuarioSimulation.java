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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class PerfilUsuarioSimulation extends Simulation {

  public PerfilUsuarioSimulation() {
    Entorno.Escala escala = Entorno.escala();

    Entorno.Usuario usuario = Entorno.usuarioNuevo();
    String cuerpoRegistro = usuario.cuerpo();

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

    ScenarioBuilder perfil = scenario("Perfil")
        .forever()
        .on(
            pace(Duration.ofSeconds(escala.paceSeconds()))
                .exec(
                    http("GET /api/v1/usuario/{id}")
                        .get(session -> "/api/v1/usuario/" + usuarioId.get())
                        .header("Authorization", session -> "Bearer " + token.get())
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$.id").ofLong().is(session -> usuarioId.get()))
                        .check(
                            jsonPath("$.nombre")
                                .ofString()
                                .transform(texto -> !texto.isBlank())
                                .is(true))
                        .check(jsonPath("$.email").is(usuario.email()))
                        .check(jsonPath("$.rol").is("USER"))));

    setUp(
            registro.injectOpen(atOnceUsers(1))
                .andThen(Entorno.escalera(perfil, escala)))
        .protocols(Entorno.protocolo())
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(escala.totalSeconds() + 5));
  }
}
