package sdet;

import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;

public class HealthBreakpointSimulation extends Simulation {

  public HealthBreakpointSimulation() {
    Entorno.Escala escala = Entorno.escala();

    ScenarioBuilder health = scenario("Health")
        .forever()
        .on(
            pace(Duration.ofSeconds(escala.paceSeconds()))
                .exec(
                    http("GET /actuator/health")
                        .get("/actuator/health")
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$.status").is("UP"))));

    setUp(Entorno.escalera(health, escala))
        .protocols(Entorno.protocolo())
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(escala.totalSeconds()));
  }
}
