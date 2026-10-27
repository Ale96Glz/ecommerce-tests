package sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;

@Epic("Plataforma E-commerce")
@Feature("Identidad")
@Severity(SeverityLevel.CRITICAL)
class IdentidadTest extends GatewayTest {
  @Test
  @Story("El registro crea un usuario")
  @DisplayName("El registro crea un usuario")
  @Description("El registro responde 201 con userId numérico, email, rol USER y token.")
  void registroCreaUnUsuario() {
    String email = emailNuevo();
    esUsuario(postIdentidad(REGISTRO, usuario(email, CONTRASENA)), 201, email);
  }

  @Test
  @Story("El inicio de sesión devuelve un token")
  @DisplayName("El inicio de sesión devuelve un token")
  @Description("El login responde 200 con userId numérico, email, rol USER y token.")
  void inicioDeSesionDevuelveUnToken() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);

    esUsuario(postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA)), 200, email);
  }

  @Test
  @Story("Una contraseña incorrecta recibe 401")
  @DisplayName("Una contraseña incorrecta recibe 401")
  @Description("Una contraseña incorrecta responde 401 con el mensaje Credenciales invalidas.")
  void contrasenaIncorrectaRecibe401() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);

    postIdentidad(LOGIN, Map.of("email", email, "password", "clave-mala"))
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Credenciales invalidas"));
  }

  @Test
  @Story("Un usuario no puede crear una categoría")
  @DisplayName("Un usuario no puede crear una categoría")
  @Description("Un USER que crea una categoría responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeCrearUnaCategoria() {
    String email = emailNuevo();
    postIdentidad(REGISTRO, usuario(email, CONTRASENA)).then().statusCode(201);
    String token = postIdentidad(LOGIN, Map.of("email", email, "password", CONTRASENA))
        .then()
        .statusCode(200)
        .extract()
        .path("token");

    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Sdet Cat", "descripcion", "Prueba"))
        .when()
        .post("/api/v1/categoria")
        .then()
        .statusCode(403)
        .contentType(containsString("json"))
        .body("status", equalTo(403))
        .body("error", equalTo("Forbidden"))
        .body("message", equalTo("Se requiere rol ADMIN"));
  }

  @Test
  @Story("Un email inválido recibe 400")
  @DisplayName("Un email inválido recibe 400")
  @Description("Un email inválido responde 400 con el mensaje email: El email no es valido.")
  void emailInvalidoRecibe400() {
    postIdentidad(REGISTRO, usuario("no-es-email", CONTRASENA))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("email: El email no es valido"));
  }

  @Test
  @Story("Un registro con email vacío recibe 400")
  @DisplayName("Un registro con email vacío recibe 400")
  @Description("Un registro con email vacío responde 400 con el mensaje email: El email no puede estar vacio.")
  void registroConEmailVacioRecibe400() {
    postIdentidad(REGISTRO, usuario("", CONTRASENA))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("email: El email no puede estar vacio"));
  }

  @Test
  @Story("Una contraseña corta recibe 400")
  @DisplayName("Una contraseña corta recibe 400")
  @Description("Una contraseña de menos de 8 caracteres responde 400 con el mensaje password: La contraseña debe tener entre 8 y 72 caracteres.")
  void contrasenaCortaRecibe400() {
    postIdentidad(REGISTRO, usuario(emailNuevo(), "corta"))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("password: La contraseña debe tener entre 8 y 72 caracteres"));
  }

  @Test
  @Story("Un email repetido recibe 409")
  @DisplayName("Un email repetido recibe 409")
  @Description("Registrar el mismo email por segunda vez responde 409.")
  void emailRepetidoRecibe409() {
    String email = emailNuevo();
    Map<String, String> cuerpo = usuario(email, CONTRASENA);
    postIdentidad(REGISTRO, cuerpo).then().statusCode(201);

    postIdentidad(REGISTRO, cuerpo)
        .then()
        .statusCode(409)
        .contentType(containsString("json"))
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo("Ya existe un usuario con el email: " + email));
  }

  @Test
  @Story("Un nombre corto recibe 400")
  @DisplayName("Un nombre corto recibe 400")
  @Description("Un nombre de un carácter responde 400 con el mensaje nombre: El nombre debe tener entre 2 y 100 caracteres.")
  void nombreCortoRecibe400() {
    postIdentidad(REGISTRO, Map.of("nombre", "A", "email", emailNuevo(), "password", CONTRASENA))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("nombre: El nombre debe tener entre 2 y 100 caracteres"));
  }

  @Test
  @Story("Una contraseña larga recibe 400")
  @DisplayName("Una contraseña larga recibe 400")
  @Description("Una contraseña de más de 72 caracteres responde 400 con el mensaje password: La contraseña debe tener entre 8 y 72 caracteres.")
  void contrasenaLargaRecibe400() {
    postIdentidad(REGISTRO, usuario(emailNuevo(), "x".repeat(73)))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("password: La contraseña debe tener entre 8 y 72 caracteres"));
  }

  @Test
  @Story("Un login con email inválido recibe 400")
  @DisplayName("Un login con email inválido recibe 400")
  @Description("Un login con email inválido responde 400 con el mensaje email: El email no es valido.")
  void loginConEmailInvalidoRecibe400() {
    postIdentidad(LOGIN, Map.of("email", "no-es-email", "password", CONTRASENA))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("email: El email no es valido"));
  }

  @Test
  @Story("Un login con contraseña vacía recibe 400")
  @DisplayName("Un login con contraseña vacía recibe 400")
  @Description("Un login con contraseña vacía responde 400 con el mensaje password: La contraseña no puede estar vacia.")
  void loginConContrasenaVaciaRecibe400() {
    postIdentidad(LOGIN, Map.of("email", emailNuevo(), "password", ""))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("password: La contraseña no puede estar vacia"));
  }

  @Test
  @Story("Un login con email vacío recibe 400")
  @DisplayName("Un login con email vacío recibe 400")
  @Description("Un login con email vacío responde 400 con el mensaje email: El email no puede estar vacio.")
  void loginConEmailVacioRecibe400() {
    postIdentidad(LOGIN, Map.of("email", "", "password", CONTRASENA))
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("email: El email no puede estar vacio"));
  }

  @Test
  @Story("Un email desconocido recibe 401")
  @DisplayName("Un email desconocido recibe 401")
  @Description("Un login de un email no registrado responde 401 con el mensaje Credenciales invalidas.")
  void emailDesconocidoRecibe401() {
    postIdentidad(LOGIN, Map.of("email", emailNuevo(), "password", CONTRASENA))
        .then()
        .statusCode(401)
        .contentType(containsString("json"))
        .body("status", equalTo(401))
        .body("error", equalTo("Unauthorized"))
        .body("message", equalTo("Credenciales invalidas"));
  }

  @Test
  @Story("Listar usuarios sin token recibe 401")
  @DisplayName("Listar usuarios sin token recibe 401")
  @Description("GET /api/v1/usuario sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void listarUsuariosSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/usuario"));
  }

  @Test
  @Story("Ver un perfil sin token recibe 401")
  @DisplayName("Ver un perfil sin token recibe 401")
  @Description("GET /api/v1/usuario/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void verPerfilSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/usuario/" + AJENO));
  }

  @Test
  @Story("Comprobar existencia sin token recibe 401")
  @DisplayName("Comprobar existencia sin token recibe 401")
  @Description("GET /api/v1/usuario/existe/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void comprobarExistenciaSinTokenRecibe401() {
    sinBearer(given().when().get("/api/v1/usuario/existe/" + AJENO));
  }

  @Test
  @Story("Un usuario no puede listar los usuarios")
  @DisplayName("Un usuario no puede listar los usuarios")
  @Description("Un USER que lista GET /api/v1/usuario responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeListarLosUsuarios() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/usuario"));
  }

  @Test
  @Story("Un usuario no puede paginar los usuarios")
  @DisplayName("Un usuario no puede paginar los usuarios")
  @Description("Un USER que lista GET /api/v1/usuario/pageable responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedePaginarLosUsuarios() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/usuario/pageable"));
  }

  @Test
  @Story("Un usuario ve su perfil")
  @DisplayName("Un usuario ve su perfil")
  @Description("GET /api/v1/usuario/{id} con el token de ese usuario responde 200 con id, nombre, email y rol USER.")
  void usuarioVeSuPerfil() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/usuario/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(sesion.id()))
        .body("nombre", equalTo("Prueba Sdet"))
        .body("email", equalTo(sesion.email()))
        .body("rol", equalTo("USER"));
  }

  @Test
  @Story("Un usuario no puede ver otro perfil")
  @DisplayName("Un usuario no puede ver otro perfil")
  @Description("GET /api/v1/usuario/{id} de otro id responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeVerOtroPerfil() {
    recursoAjeno(given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/usuario/" + AJENO));
  }

  @Test
  @Story("Un usuario cambia su nombre")
  @DisplayName("Un usuario cambia su nombre")
  @Description("PUT /api/v1/usuario/{id} con el token de ese usuario responde 200 con el nombre nuevo.")
  void usuarioCambiaSuNombre() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Nombre Nuevo", "email", sesion.email()))
        .when()
        .put("/api/v1/usuario/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(sesion.id()))
        .body("nombre", equalTo("Nombre Nuevo"))
        .body("email", equalTo(sesion.email()))
        .body("rol", equalTo("USER"));
  }

  @Test
  @Story("Un usuario no puede editar otro perfil")
  @DisplayName("Un usuario no puede editar otro perfil")
  @Description("PUT /api/v1/usuario/{id} de otro id responde 403 con el mensaje No puedes acceder a recursos de otro usuario.")
  void usuarioNoPuedeEditarOtroPerfil() {
    recursoAjeno(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Nombre Nuevo", "email", emailNuevo()))
        .when()
        .put("/api/v1/usuario/" + AJENO));
  }

  @Test
  @Story("Editar el perfil con un nombre corto recibe 400")
  @DisplayName("Editar el perfil con un nombre corto recibe 400")
  @Description("PUT /api/v1/usuario/{id} con un nombre de un carácter responde 400 con el mensaje nombre: El nombre debe tener entre 2 y 100 caracteres.")
  void nombreCortoAlEditarRecibe400() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "A", "email", sesion.email()))
        .when()
        .put("/api/v1/usuario/" + sesion.id())
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("nombre: El nombre debe tener entre 2 y 100 caracteres"));
  }

  @Test
  @Story("Editar el perfil con un email inválido recibe 400")
  @DisplayName("Editar el perfil con un email inválido recibe 400")
  @Description("PUT /api/v1/usuario/{id} con un email inválido responde 400 con el mensaje email: El email no es valido.")
  void emailInvalidoAlEditarRecibe400() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Prueba Sdet", "email", "no-es-email"))
        .when()
        .put("/api/v1/usuario/" + sesion.id())
        .then()
        .statusCode(400)
        .contentType(containsString("json"))
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("email: El email no es valido"));
  }

  @Test
  @Story("Editar el perfil con un email repetido recibe 409")
  @DisplayName("Editar el perfil con un email repetido recibe 409")
  @Description("PUT /api/v1/usuario/{id} con el email de otro usuario responde 409 con el mensaje Ya existe un usuario con el email.")
  void emailRepetidoAlEditarRecibe409() {
    Sesion dueno = sesionNueva();
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Prueba Sdet", "email", dueno.email()))
        .when()
        .put("/api/v1/usuario/" + sesion.id())
        .then()
        .statusCode(409)
        .contentType(containsString("json"))
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo("Ya existe un usuario con el email: " + dueno.email()));
  }

  @Test
  @Story("Un usuario cambia su email")
  @DisplayName("Un usuario cambia su email")
  @Description("PUT /api/v1/usuario/{id} con un email nuevo responde 200 con ese email.")
  void usuarioCambiaSuEmail() {
    Sesion sesion = sesionNueva();
    String email = emailNuevo();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Prueba Sdet", "email", email))
        .when()
        .put("/api/v1/usuario/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(sesion.id()))
        .body("nombre", equalTo("Prueba Sdet"))
        .body("email", equalTo(email))
        .body("rol", equalTo("USER"));
  }

  @Test
  @Story("Editar el perfil sin token recibe 401")
  @DisplayName("Editar el perfil sin token recibe 401")
  @Description("PUT /api/v1/usuario/{id} sin Authorization responde 401 con el mensaje Falta el header Authorization Bearer.")
  void editarPerfilSinTokenRecibe401() {
    sinBearer(given()
        .contentType(ContentType.JSON)
        .body(Map.of("nombre", "Prueba Sdet", "email", emailNuevo()))
        .when()
        .put("/api/v1/usuario/" + AJENO));
  }

  @Test
  @Story("Un usuario comprueba que existe")
  @DisplayName("Un usuario comprueba que existe")
  @Description("GET /api/v1/usuario/existe/{id} responde 200 con el id y el rol USER.")
  void usuarioCompruebaQueExiste() {
    Sesion sesion = sesionNueva();
    given()
        .header("Authorization", "Bearer " + sesion.token())
        .when()
        .get("/api/v1/usuario/existe/" + sesion.id())
        .then()
        .statusCode(200)
        .contentType(containsString("json"))
        .body("id", equalTo(sesion.id()))
        .body("rol", equalTo("USER"));
  }

  @Test
  @Story("Un usuario inexistente recibe 404")
  @DisplayName("Un usuario inexistente recibe 404")
  @Description("GET /api/v1/usuario/existe/{id} de un id ausente responde 404 con el mensaje No existe un usuario válido con id.")
  void usuarioInexistenteRecibe404() {
    noEncontrado(
        given().header("Authorization", "Bearer " + sesionNueva().token()).when().get("/api/v1/usuario/existe/" + AJENO),
        "No existe un usuario válido con id: " + AJENO);
  }

  @Test
  @Story("Un usuario no puede cambiar un rol")
  @DisplayName("Un usuario no puede cambiar un rol")
  @Description("PUT /api/v1/usuario/{id}/rol como USER responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeCambiarUnRol() {
    soloAdmin(given()
        .header("Authorization", "Bearer " + sesionNueva().token())
        .when()
        .put("/api/v1/usuario/" + AJENO + "/rol?rol=ADMIN"));
  }

  @Test
  @Story("Un usuario no puede borrar un usuario")
  @DisplayName("Un usuario no puede borrar un usuario")
  @Description("DELETE /api/v1/usuario/{id} como USER responde 403 con el mensaje Se requiere rol ADMIN.")
  void usuarioNoPuedeBorrarUnUsuario() {
    soloAdmin(given().header("Authorization", "Bearer " + sesionNueva().token()).when().delete("/api/v1/usuario/" + AJENO));
  }

  @Step("Comprobar la forma del usuario")
  private static void esUsuario(Response respuesta, int estado, String email) {
    respuesta.then()
        .statusCode(estado)
        .contentType(containsString("json"))
        .body("email", equalTo(email))
        .body("rol", equalTo("USER"))
        .body("userId", instanceOf(Number.class))
        .body("token.length()", greaterThan(0));
  }
}
