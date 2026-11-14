package sdet;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;

public class CheckoutSagaSimulation extends Simulation {

  public CheckoutSagaSimulation() {
    int usuarios = Integer.getInteger("load.users", 20);
    int rampaSegundos = Integer.getInteger("load.ramp.seconds", 60);

    ScenarioBuilder saga = scenario("Checkout saga")
        .exec(
            http("Registro")
                .post("/api/v1/auth/register")
                .body(StringBody(session -> Entorno.usuarioNuevo().cuerpo()))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(201))
                .check(jsonPath("$.userId").ofLong().exists())
                .check(jsonPath("$.rol").is("USER"))
                .check(jsonPath("$.token").saveAs("token")))
        .exitHereIfFailed()
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Listado")
                .get("/api/v1/producto")
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().is(200))
                .check(Productos.pagable()))
        .exitHereIfFailed()
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Crear pedido")
                .post("/api/v1/pedido")
                .header("Authorization", "Bearer #{token}")
                .body(StringBody("{\"items\":[{\"productoId\":#{productoId},\"cantidad\":1}]}"))
                .asJson()
                .requestTimeout(Duration.ofSeconds(3))
                .check(status().in(201, 409))
                .check(status().saveAs("crearStatus"))
                .check(jsonPath("$.id").optional().saveAs("pedidoId")))
        .exitHereIfFailed()
        .doIf(session -> session.getInt("crearStatus") == 201)
        .then(
            exec(session -> session.set("estadoPedido", "CREADO"))
                .asLongAsDuring(
                    session -> "CREADO".equals(session.getString("estadoPedido")),
                    Duration.ofSeconds(20))
                .on(
                    pause(Duration.ofSeconds(1))
                        .exec(
                            http("Estado del pedido")
                                .get("/api/v1/pedido/#{pedidoId}")
                                .header("Authorization", "Bearer #{token}")
                                .requestTimeout(Duration.ofSeconds(3))
                                .check(status().is(200))
                                .check(jsonPath("$.estado").saveAs("estadoPedido"))))
                .exec(
                    http("Pedido cerrado")
                        .get("/api/v1/pedido/#{pedidoId}")
                        .header("Authorization", "Bearer #{token}")
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$.estado").is("PAGADO")))
                .exitHereIfFailed()
                .pause(Duration.ofSeconds(2))
                .exec(
                    http("Mis notificaciones")
                        .get("/api/v1/notificacion/mias")
                        .header("Authorization", "Bearer #{token}")
                        .requestTimeout(Duration.ofSeconds(3))
                        .check(status().is(200))
                        .check(jsonPath("$[?(@.tipo == 'PEDIDO_CREADO')].tipo").find().exists())
                        .check(jsonPath("$[?(@.tipo == 'PAGO_PROCESADO')].tipo").find().exists())));

    setUp(saga.injectOpen(rampUsers(usuarios).during(Duration.ofSeconds(rampaSegundos))))
        .protocols(Entorno.protocolo())
        .maxDuration(Duration.ofSeconds(rampaSegundos + 45))
        .assertions(
            global().responseTime().max().lt(3000),
            global().failedRequests().percent().lt(5.0),
            details("Pedido cerrado").successfulRequests().count().gt(0L));
  }
}
