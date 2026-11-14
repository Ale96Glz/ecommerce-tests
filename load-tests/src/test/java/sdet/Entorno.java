package sdet;

import static io.gatling.javaapi.core.CoreDsl.incrementConcurrentUsers;
import static io.gatling.javaapi.http.HttpDsl.http;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

/** Configuración común de las simulaciones: URL base, protocolo y escalera de usuarios. */
final class Entorno {
  private Entorno() {}

  static HttpProtocolBuilder protocolo() {
    String baseUrl = System.getenv("BASE_URL");
    if (baseUrl == null || baseUrl.isBlank()) {
      baseUrl = "https://ecommerce.local";
    }
    return http.baseUrl(baseUrl).shareConnections().disableWarmUp();
  }

  static Escala escala() {
    int paceSeconds = Integer.getInteger("paceSeconds", 1);
    int stepUsers = Integer.getInteger("stepUsers", 5);
    int levels = Math.max(Integer.getInteger("levels", 4), 2);
    int levelSeconds = Math.max(Integer.getInteger("levelSeconds", 10), paceSeconds);
    int rampSeconds = 1;
    int totalSeconds = levels * levelSeconds + (levels - 1) * rampSeconds + 2;
    return new Escala(paceSeconds, stepUsers, levels, levelSeconds, rampSeconds, totalSeconds);
  }

  /** Inyección cerrada que sube de a {@code stepUsers} hasta {@code levels} niveles. */
  static PopulationBuilder escalera(ScenarioBuilder escenario, Escala escala) {
    return escenario.injectClosed(
        incrementConcurrentUsers(escala.stepUsers())
            .times(escala.levels())
            .eachLevelLasting(Duration.ofSeconds(escala.levelSeconds()))
            .separatedByRampsLasting(Duration.ofSeconds(escala.rampSeconds()))
            .startingFrom(escala.stepUsers()));
  }

  record Escala(int paceSeconds, int stepUsers, int levels, int levelSeconds, int rampSeconds,
      int totalSeconds) {}
}
