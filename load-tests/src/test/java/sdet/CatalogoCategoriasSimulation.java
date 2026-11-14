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

public class CatalogoCategoriasSimulation extends Simulation {

  public CatalogoCategoriasSimulation() {
    Entorno.Escala escala = Entorno.escala();

    ScenarioBuilder catalogo = scenario("Categorias")
        .forever()
        .on(
            pace(Duration.ofSeconds(escala.paceSeconds()))
                .exec(
                    http("GET /api/v1/categoria")
                        .get("/api/v1/categoria")
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$[0].id").ofLong().exists())
                        .check(
                            jsonPath("$[0].nombre")
                                .ofString()
                                .transform(nombre -> !nombre.isBlank())
                                .is(true))));

    setUp(Entorno.escalera(catalogo, escala))
        .protocols(Entorno.protocolo())
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(escala.totalSeconds()));
  }
}
