package createCourierTests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.*;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;

public class CourierCreateTests {
    private static CourierCreds courierCreds;
    private static Integer courierId;

    @BeforeEach
    public void setupConnection(){
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        courierCreds = new CourierCreds(RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20));
        courierId = null;
    }

    @Test
    @DisplayName("Курьера можно создать")
    public void canCreateCourier(){
        Response response = RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(courierCreds)
                .when()
                .post("/api/v1/courier");
        response.then().assertThat().statusCode(201)
                .and().body("ok", equalTo(true));

        courierId = RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(String.format("{\"login\": \"%s\", \"password\": \"%s\"}",
                        courierCreds.getLogin(), courierCreds.getPassword()))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .extract().path("id");
    }

    @Test
    @DisplayName("Чтобы создать курьера, нужно передать в ручку все обязательные поля. Тест без логина")
    public void withoutLogin(){
        HashMap<String, String> withoutLogin = new HashMap<>();
        withoutLogin.put("password", courierCreds.getPassword());
        withoutLogin.put("firstName", courierCreds.getFirstName());

        RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(withoutLogin)
                .when()
                .post("/api/v1/courier")
                .then()
                .assertThat().statusCode(400);
    }

    @Test
    @DisplayName("Чтобы создать курьера, нужно передать в ручку все обязательные поля. Тест без пароля")
    public void withoutPassword(){

        HashMap<String, String> withoutPassword = new HashMap<>();
        withoutPassword.put("login", courierCreds.getLogin());
        withoutPassword.put("firstName", courierCreds.getFirstName());

        RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(withoutPassword)
                .when()
                .post("/api/v1/courier")
                .then()
                .assertThat().statusCode(400)
                .and();
    }



    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    public void cannotCreateTwoIdentCouriers(){
        CourierCreds courierSecond = new CourierCreds(courierCreds.getLogin(),
                courierCreds.getPassword(),
                courierCreds.getFirstName());

        RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(courierCreds)
                .when()
                .post("/api/v1/courier");

        RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(courierSecond)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409);
    }

    @AfterEach
    public void deleteCourier(){
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
