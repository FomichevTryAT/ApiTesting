package loginCourierTests;

import createCourierTests.CourierCreds;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class CourierAuthorizationTests {
    private static CourierCreds courierCreds;
    private static Integer courierId;
    private static String login;
    private static String password;
    RestAssuredConfig config = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", 5000)
                    .setParam("http.socket.timeout", 5000));

    @BeforeAll
    public static void initCourierAndConn() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        courierCreds = new CourierCreds(RandomStringGenerator.builder().withinRange('a', 'z').get().generate(15),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(15),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(15));

        Response response = RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(courierCreds)
                .when()
                .post("/api/v1/courier");
        response.then().assertThat().statusCode(201);

        login = courierCreds.getLogin();
        password = courierCreds.getPassword();
    }

    @Test
    @DisplayName("Курьер может авторизоваться")
    public void courierCanAuthorization() {
        HashMap<String, String> logoPass = new HashMap<>();
        logoPass.put("login", login);
        logoPass.put("password", password);

        Response response = RestAssured.given()
                .config(config)
                .header("Content-type", "application/json")
                .body(logoPass)
                .when()
                .post("/api/v1/courier/login");

        response.then().statusCode(200).and().body("id", notNullValue());

        courierId = response.then().extract().path("id");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Для авторизации надо передать все обязательные поля. Тест без пароля")
    public void authorizationWithoutLogin() {
        RestAssured.given()
                .header("Content-type", "application/json")
                .body("{\"login\":\"" + login + "\"}")
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    @Timeout(value = 6, unit = TimeUnit.SECONDS)
    @DisplayName("Для авторизации надо передать все обязательные поля. Тест без логина")
    public void authorizationWithoutPassword() {
        HashMap<String, String> onlyLogin = new HashMap<>();
        onlyLogin.put("login", login);

        RestAssured.given()
                .header("Content-type", "application/json")
                .body(onlyLogin)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .log().all()
                .assertThat()
                .statusCode(400);
    }

    @Test
    @DisplayName("Тест неверного указания логина + проверка несуществующего пользователя")
    public void authorizationWithIncorrectLogin(){
        HashMap<String, String> incorrectLogin = new HashMap<>();
        incorrectLogin.put("login", login + login);
        incorrectLogin.put("password", password);

        RestAssured.given()
                .config(config)
                .header("Content-type", "application/json")
                .body(incorrectLogin)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat().statusCode(404);
    }

    @Test
    @DisplayName("Тест неверного указания пароля")
    public void authorizationWithIncorrectPassword(){
        HashMap<String, String> incorrectLogin = new HashMap<>();
        incorrectLogin.put("login", login);
        incorrectLogin.put("password", password + password);

        RestAssured.given()
                .config(config)
                .header("Content-type", "application/json")
                .body(incorrectLogin)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat().statusCode(404).and().body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    public void checkId(){
        Assertions.assertNotNull(courierId);
    }

    @AfterAll
    public static void deleteCourier(){
        if (courierId != null) {
            RestAssured.given()
                    .when()
                    .delete(String.format("/api/v1/courier/%d", courierId))
                    .then()
                    .assertThat()
                    .statusCode(200);
        }
    }
}
