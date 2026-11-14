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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class StockClientesSimulation extends Simulation {

  public StockClientesSimulation() {
    int usuarios = Math.max(Integer.getInteger("load.users", 20), 2);
    int ritmoRegistro = Math.max(Integer.getInteger("paceSeconds", 1), 1);

    AtomicLong productoId = new AtomicLong();
    Queue<String> tokens = new ConcurrentLinkedQueue<>();

    ScenarioBuilder preparacion = scenario("Preparacion")
        .exec(
            http("GET /api/v1/producto")
                .get("/api/v1/producto")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(Productos.conStock()))
        .exec(session -> {
          productoId.set(session.getLong("productoId"));
          return session;
        })
        .repeat(usuarios)
        .on(
            pace(Duration.ofSeconds(ritmoRegistro))
                .exec(
                    http("Registro")
                        .post("/api/v1/auth/register")
                        .body(StringBody(session -> Entorno.usuarioNuevo().cuerpo()))
                        .asJson()
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(201))
                        .check(jsonPath("$.token").saveAs("token")))
                .exitHereIfFailed()
                .exec(session -> {
                  tokens.add(session.getString("token"));
                  return session;
                }));

    ScenarioBuilder compra = scenario("Stock clientes")
        .exec(session -> {
          String token = tokens.poll();
          if (token == null || token.isBlank()) {
            return session.markAsFailed();
          }
          return session.set("token", token);
        })
        .exitHereIfFailed()
        .exec(
            http("POST /api/v1/pedido")
                .post("/api/v1/pedido")
                .header("Authorization", "Bearer #{token}")
                .body(StringBody(session ->
                    "{\"items\":[{\"productoId\":" + productoId.get() + ",\"cantidad\":1}]}"))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().in(201, 409)));

    setUp(
            preparacion.injectOpen(atOnceUsers(1))
                .andThen(compra.injectOpen(atOnceUsers(usuarios))))
        .protocols(Entorno.protocolo())
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0))
        .maxDuration(Duration.ofSeconds((long) usuarios * ritmoRegistro + 20));
  }
}
