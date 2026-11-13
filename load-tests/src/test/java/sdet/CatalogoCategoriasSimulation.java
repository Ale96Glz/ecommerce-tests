package sdet;

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

public class CatalogoCategoriasSimulation extends Simulation {

  public CatalogoCategoriasSimulation() {
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

    HttpProtocolBuilder protocolo = http
        .baseUrl(baseUrl)
        .shareConnections()
        .disableWarmUp();

    ScenarioBuilder catalogo = scenario("Categorias")
        .forever()
        .on(
            pace(Duration.ofSeconds(paceSeconds))
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

    setUp(
            catalogo.injectClosed(
                incrementConcurrentUsers(stepUsers)
                    .times(levels)
                    .eachLevelLasting(Duration.ofSeconds(levelSeconds))
                    .separatedByRampsLasting(Duration.ofSeconds(rampSeconds))
                    .startingFrom(stepUsers)))
        .protocols(protocolo)
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(totalSeconds));
  }
}
