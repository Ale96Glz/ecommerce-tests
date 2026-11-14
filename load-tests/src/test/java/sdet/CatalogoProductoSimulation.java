package sdet;

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

public class CatalogoProductoSimulation extends Simulation {

  public CatalogoProductoSimulation() {
    Entorno.Escala escala = Entorno.escala();

    AtomicLong productoId = new AtomicLong();

    ScenarioBuilder listado = scenario("Listado")
        .exec(
            http("GET /api/v1/producto")
                .get("/api/v1/producto")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(Productos.conStock()))
        .exec(session -> {
          productoId.set(session.getLong("productoId"));
          return session;
        });

    ScenarioBuilder producto = scenario("Producto")
        .forever()
        .on(
            pace(Duration.ofSeconds(escala.paceSeconds()))
                .exec(
                    http("GET /api/v1/producto/{id}")
                        .get(session -> "/api/v1/producto/" + productoId.get())
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$.id").ofLong().is(session -> productoId.get()))
                        .check(
                            jsonPath("$.nombre")
                                .ofString()
                                .transform(nombre -> !nombre.isBlank())
                                .is(true))));

    setUp(
            listado.injectOpen(atOnceUsers(1))
                .andThen(Entorno.escalera(producto, escala)))
        .protocols(Entorno.protocolo())
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds(escala.totalSeconds() + 3));
  }
}
