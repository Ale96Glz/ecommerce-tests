package sdet;

import static io.gatling.javaapi.core.CoreDsl.incrementConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

public class CatalogoProductosPageableSimulation extends Simulation {

  public CatalogoProductosPageableSimulation() {
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

    ScenarioBuilder catalogo = scenario("Catalogo pageable")
        .forever()
        .on(
            pace(Duration.ofSeconds(paceSeconds))
                .exec(
                    http("GET /api/v1/producto/pageable")
                        .get("/api/v1/producto/pageable?page=0&size=1")
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$.page").ofInt().is(0))
                        .check(jsonPath("$.size").ofInt().is(1))
                        .check(jsonPath("$.content[0].id").ofLong().exists())
                        .check(
                            jsonPath("$.content[0].nombre")
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
        .maxDuration(Duration.ofSeconds(totalSeconds));
  }
}
