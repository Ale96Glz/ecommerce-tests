package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@Epic("Plataforma E-commerce")
@Feature("Notificaciones")
@Severity(SeverityLevel.NORMAL)
class NotificacionesTest extends GatewayTest {
  @Test
  @Story("Una notificación sin token recibe 401")
  @DisplayName("Una notificación sin token recibe 401")
  @Description("GET /api/v1/notificacion/mias sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void notificacionSinTokenRecibe401() {
    given()
        .when()
        .get("/api/v1/notificacion/mias")
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Falta el header Authorization Bearer"));
  }

  @Test
  @Story("Un usuario ve sus notificaciones")
  @DisplayName("Un usuario ve sus notificaciones")
  @Description("GET /api/v1/notificacion/mias con token de USER responde 200 con un arreglo.")
  void usuarioVeSusNotificaciones() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");

    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/api/v1/notificacion/mias")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede listar todas las notificaciones")
  @DisplayName("Un usuario no puede listar todas las notificaciones")
  @Description("Un USER que lista GET /api/v1/notificacion responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeListarTodasLasNotificaciones() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/notificacion"));
  }

  @Test
  @Story("Un usuario no puede paginar las notificaciones")
  @DisplayName("Un usuario no puede paginar las notificaciones")
  @Description("Un USER que lista GET /api/v1/notificacion/pageable responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedePaginarLasNotificaciones() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/notificacion/pageable"));
  }

  @Test
  @Story("Listar notificaciones sin token recibe 401")
  @DisplayName("Listar notificaciones sin token recibe 401")
  @Description("GET /api/v1/notificacion sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void listarNotificacionesSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/notificacion"));
  }

  @Test
  @Story("Paginar notificaciones sin token recibe 401")
  @DisplayName("Paginar notificaciones sin token recibe 401")
  @Description("GET /api/v1/notificacion/pageable sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void paginarNotificacionesSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/notificacion/pageable"));
  }

  @Test
  @Story("Una notificación inexistente recibe 404")
  @DisplayName("Una notificación inexistente recibe 404")
  @Description("GET /api/v1/notificacion/{id} de un id ausente responde 404 con el mensaje No se encontró la notificación con id.")
  void notificacionInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/notificacion/" + AJENO),
        "No se encontró la notificación con id: " + AJENO);
  }

  @Test
  @Story("Una notificación por id sin token recibe 401")
  @DisplayName("Una notificación por id sin token recibe 401")
  @Description("GET /api/v1/notificacion/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void notificacionPorIdSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/notificacion/" + AJENO));
  }

  @Test
  @Story("Un usuario ve las notificaciones de su id")
  @DisplayName("Un usuario ve las notificaciones de su id")
  @Description("GET /api/v1/notificacion/usuario/{id} con el token de ese usuario responde 200 con un arreglo.")
  void usuarioVeLasNotificacionesDeSuId() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/usuario/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede ver las notificaciones de otro")
  @DisplayName("Un usuario no puede ver las notificaciones de otro")
  @Description("GET /api/v1/notificacion/usuario/{id} de otro id responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerLasNotificacionesDeOtro() {
    recursoAjeno(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .get("/api/v1/notificacion/usuario/" + AJENO));
  }

  @Test
  @Story("Un usuario ve sus notificaciones no leídas")
  @DisplayName("Un usuario ve sus notificaciones no leídas")
  @Description("GET /api/v1/notificacion/usuario/{id}/no-leidas con el token de ese usuario responde 200 con un arreglo.")
  void usuarioVeSusNotificacionesNoLeidas() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/usuario/" + sesion.id() + "/no-leidas")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("", instanceOf(List.class));
  }

  @Test
  @Story("Un usuario no puede ver las no leídas de otro")
  @DisplayName("Un usuario no puede ver las no leídas de otro")
  @Description("GET /api/v1/notificacion/usuario/{id}/no-leidas de otro id responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerLasNoLeidasDeOtro() {
    recursoAjeno(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .get("/api/v1/notificacion/usuario/" + AJENO + "/no-leidas"));
  }

  @Test
  @Story("Marcar una notificación inexistente recibe 404")
  @DisplayName("Marcar una notificación inexistente recibe 404")
  @Description("PATCH /api/v1/notificacion/{id}/leida de un id ausente responde 404 con el mensaje No se encontró la notificación con id.")
  void marcarNotificacionInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().patch("/api/v1/notificacion/" + AJENO + "/leida"),
        "No se encontró la notificación con id: " + AJENO);
  }

  @Test
  @Story("Marcar una notificación sin token recibe 401")
  @DisplayName("Marcar una notificación sin token recibe 401")
  @Description("PATCH /api/v1/notificacion/{id}/leida sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void marcarNotificacionSinTokenRecibe401() {
    sinBearer(given().when().patch("/api/v1/notificacion/" + AJENO + "/leida"));
  }

  @Test
  @Story("Crear una notificación sin token recibe 401")
  @DisplayName("Crear una notificación sin token recibe 401")
  @Description("POST /api/v1/notificacion sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void crearNotificacionSinTokenRecibe401() {
    sinBearer(given().contentType("application/json").body(aviso()).when().post("/api/v1/notificacion"));
  }

  @Test
  @Story("Un tipo de notificación inválido recibe 400")
  @DisplayName("Un tipo de notificación inválido recibe 400")
  @Description("Un tipo desconocido responde 400 con el mensaje Tipo de notificación inválido.")
  void tipoDeNotificacionInvalidoRecibe400() {
    given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType("application/json")
        .body(Map.of("tipo", "NO-ES", "mensaje", "Prueba", "referenciaId", 1))
        .when()
        .post("/api/v1/notificacion")
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("Tipo de notificación inválido: NO-ES"));
  }

  @Test
  @Story("Un usuario crea su notificación")
  @DisplayName("Un usuario crea su notificación")
  @Description("POST /api/v1/notificacion responde 201 con el tipo, el mensaje, la referencia y leida en falso.")
  void usuarioCreaSuNotificacion() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(aviso())
        .when()
        .post("/api/v1/notificacion")
        .then()
        .statusCode(201)
        .contentType(containsString("json"))
        .body("usuarioId", equalTo(sesion.id()))
        .body("tipo", equalTo("PEDIDO_CREADO"))
        .body("mensaje", equalTo("Prueba Sdet"))
        .body("referenciaId", equalTo(1))
        .body("leida", equalTo(false))
        .body("id", instanceOf(Number.class));
  }

  @Test
  @Story("Un usuario marca su notificación como leída")
  @DisplayName("Un usuario marca su notificación como leída")
  @Description("PATCH /api/v1/notificacion/{id}/leida responde 200 y leida en verdadero.")
  void usuarioMarcaSuNotificacionComoLeida() {
    Sesion sesion = sesionNueva();
    Number id = given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(aviso())
        .when()
        .post("/api/v1/notificacion")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .patch("/api/v1/notificacion/" + id + "/leida")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(id))
        .body("leida", equalTo(true));
  }

  @Test
  @Story("Un usuario crea un aviso de pago procesado")
  @DisplayName("Un usuario crea un aviso de pago procesado")
  @Description("POST /api/v1/notificacion con tipo PAGO_PROCESADO responde 201 y leida en falso.")
  void usuarioCreaAvisoDePagoProcesado() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(Map.of("tipo", "PAGO_PROCESADO", "mensaje", "Prueba Sdet", "referenciaId", 1))
        .when()
        .post("/api/v1/notificacion")
        .then()
        .statusCode(201)
        .contentType(containsString("json"))
        .body("usuarioId", equalTo(sesion.id()))
        .body("tipo", equalTo("PAGO_PROCESADO"))
        .body("mensaje", equalTo("Prueba Sdet"))
        .body("referenciaId", equalTo(1))
        .body("leida", equalTo(false))
        .body("id", instanceOf(Number.class));
  }

  @Test
  @Story("Un usuario vuelve a marcar su notificación leída")
  @DisplayName("Un usuario vuelve a marcar su notificación leída")
  @Description("PATCH /api/v1/notificacion/{id}/leida sobre una ya leída responde 200 y leida sigue en verdadero.")
  void usuarioVuelveAMarcarSuNotificacionLeida() {
    Sesion sesion = sesionNueva();
    Number id = given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(aviso())
        .when()
        .post("/api/v1/notificacion")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .patch("/api/v1/notificacion/" + id + "/leida")
        .then()
        .statusCode(200);

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .patch("/api/v1/notificacion/" + id + "/leida")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(id))
        .body("leida", equalTo(true));
  }

  @Test
  @Story("Notificaciones propias con token inválido reciben 401")
  @DisplayName("Notificaciones propias con token inválido reciben 401")
  @Description("GET /api/v1/notificacion/mias con un Bearer inválido responde 401 con el mensaje Token inválido o expirado.")
  void notificacionesPropiasConTokenInvalidoReciben401() {
    bearerInvalido(given().header("Authorization", "Bearer token-falso").when().get("/api/v1/notificacion/mias"));
  }

  @Test
  @Story("Un usuario ve su notificación por id")
  @DisplayName("Un usuario ve su notificación por id")
  @Description("GET /api/v1/notificacion/{id} con el token del dueño responde 200 con el tipo y leida en falso.")
  void usuarioVeSuNotificacionPorId() {
    Sesion sesion = sesionNueva();
    Number id = crearAviso(sesion);

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/" + id)
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(id))
        .body("usuarioId", equalTo(sesion.id()))
        .body("tipo", equalTo("PEDIDO_CREADO"))
        .body("mensaje", equalTo("Prueba Sdet"))
        .body("leida", equalTo(false));
  }

  @Test
  @Story("Un usuario no puede ver la notificación de otro")
  @DisplayName("Un usuario no puede ver la notificación de otro")
  @Description("GET /api/v1/notificacion/{id} de otro usuario responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerLaNotificacionDeOtro() {
    Number id = crearAviso(sesionNueva());
    recursoAjeno(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .get("/api/v1/notificacion/" + id));
  }

  @Test
  @Story("Un usuario no puede marcar la notificación de otro")
  @DisplayName("Un usuario no puede marcar la notificación de otro")
  @Description("PATCH /api/v1/notificacion/{id}/leida de otro usuario responde 403 y la notificación sigue sin leerse.")
  void usuarioNoPuedeMarcarLaNotificacionDeOtro() {
    Sesion dueno = sesionNueva();
    Number id = crearAviso(dueno);
    recursoAjeno(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .patch("/api/v1/notificacion/" + id + "/leida"));

    given()
        .header("Authorization", "Bearer " + dueno.token())
        .when()
        .get("/api/v1/notificacion/" + id)
        .then()
        .statusCode(200)
        .body("leida", equalTo(false));
  }

  @Test
  @Story("La notificación creada aparece en no leídas")
  @DisplayName("La notificación creada aparece en no leídas")
  @Description("GET /api/v1/notificacion/usuario/{id}/no-leidas incluye la notificación recién creada.")
  void notificacionCreadaApareceEnNoLeidas() {
    Sesion sesion = sesionNueva();
    Number id = crearAviso(sesion);

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/usuario/" + sesion.id() + "/no-leidas")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", hasItem(id));
  }

  @Test
  @Story("La notificación leída sale de no leídas")
  @DisplayName("La notificación leída sale de no leídas")
  @Description("Tras marcarla leída, GET /api/v1/notificacion/usuario/{id}/no-leidas ya no incluye esa notificación.")
  void notificacionLeidaSaleDeNoLeidas() {
    Sesion sesion = sesionNueva();
    Number id = crearAviso(sesion);
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .patch("/api/v1/notificacion/" + id + "/leida")
        .then()
        .statusCode(200);

    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/usuario/" + sesion.id() + "/no-leidas")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", not(hasItem(id)));
  }

  @Test
  @Story("El pedido creado avisa al usuario")
  @DisplayName("El pedido creado avisa al usuario")
  @Description("GET /api/v1/notificacion/mias incluye un aviso PEDIDO_CREADO del pedido recién creado.")
  void pedidoCreadoAvisaAlUsuario() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      String aviso = "Tu pedido #" + pedidoId + " fue creado.";
      esperarAviso(sesion, "PEDIDO_CREADO", aviso);
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("El pago procesado avisa al usuario")
  @DisplayName("El pago procesado avisa al usuario")
  @Description("GET /api/v1/notificacion/mias incluye un aviso PAGO_PROCESADO con estado PROCESADO.")
  void pagoProcesadoAvisaAlUsuario() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", 10.0))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(201);
      esperarAviso(sesion, "PAGO_PROCESADO", "del pedido #" + pedidoId + " quedó en estado PROCESADO");
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  @Test
  @Story("El pago rechazado avisa al usuario")
  @DisplayName("El pago rechazado avisa al usuario")
  @Description("GET /api/v1/notificacion/mias incluye un aviso PAGO_PROCESADO con estado RECHAZADO y el motivo del tope.")
  void pagoRechazadoAvisaAlUsuario() throws InterruptedException {
    Sesion sesion = sesionNueva();
    Number productoId = productoConStock();
    Number pedidoId = null;
    try {
      pedidoId = crearPedido(sesion, productoId);
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .contentType("application/json")
          .body(Map.of("pedidoId", pedidoId, "monto", 1000.0))
          .when()
          .post("/api/v1/pago")
          .then()
          .statusCode(201);
      esperarAviso(
          sesion,
          "PAGO_PROCESADO",
          "del pedido #" + pedidoId + " quedó en estado RECHAZADO",
          "Monto excede el máximo aprobado");
    } finally {
      devolverPedido(sesion, pedidoId, productoId);
    }
  }

  private static void esperarAviso(Sesion sesion, String tipo, String... fragmentos) throws InterruptedException {
    for (int intento = 0; intento < 20 && !avisoPresente(sesion, tipo, fragmentos); intento++) {
      Thread.sleep(1000);
    }
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/mias")
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("findAll { it.tipo == '" + tipo + "' }.mensaje", hasItem(contiene(fragmentos)));
  }

  private static boolean avisoPresente(Sesion sesion, String tipo, String... fragmentos) {
    List<String> mensajes = given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/notificacion/mias")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("findAll { it.tipo == '" + tipo + "' }.mensaje");
    if (mensajes == null) {
      return false;
    }
    for (String mensaje : mensajes) {
      if (mensaje != null && java.util.Arrays.stream(fragmentos).allMatch(mensaje::contains)) {
        return true;
      }
    }
    return false;
  }

  private static org.hamcrest.Matcher<String> contiene(String... fragmentos) {
    if (fragmentos.length == 1) {
      return containsString(fragmentos[0]);
    }
    return allOf(containsString(fragmentos[0]), containsString(fragmentos[1]));
  }

  private static Number productoConStock() {
    List<Map<String, Object>> productos = given()
        .when()
        .get("/api/v1/producto")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("");
    for (Map<String, Object> producto : productos) {
      int stock = ((Number) producto.get("stock")).intValue();
      if (stock >= 1 && !"AGOTADO".equals(String.valueOf(producto.get("estado")))) {
        return (Number) producto.get("id");
      }
    }
    throw new IllegalStateException("No hay un producto con stock");
  }

  private static Number crearPedido(Sesion sesion, Number productoId) {
    return given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(Map.of("items", List.of(Map.of("productoId", productoId, "cantidad", 1))))
        .when()
        .post("/api/v1/pedido")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  private static void devolverPedido(Sesion sesion, Number pedidoId, Number productoId) {
    if (pedidoId == null) {
      return;
    }
    Response respuesta = given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .put("/api/v1/pedido/" + pedidoId + "/cancelar");
    if (respuesta.statusCode() == 200) {
      return;
    }
    String mensaje = respuesta.path("message");
    if (mensaje != null && mensaje.contains("PAGADO")) {
      given()
          .header("Authorization", "Bearer " + sesion.token())
          .when()
          .put("/api/v1/producto/" + productoId + "/stock/reponer?cantidad=1")
          .then()
          .statusCode(200);
    }
  }

  private static Map<String, Object> aviso() {
    return Map.of("tipo", "PEDIDO_CREADO", "mensaje", "Prueba Sdet", "referenciaId", 1);
  }

  private static Number crearAviso(Sesion sesion) {
    return given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType("application/json")
        .body(aviso())
        .when()
        .post("/api/v1/notificacion")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }
}
