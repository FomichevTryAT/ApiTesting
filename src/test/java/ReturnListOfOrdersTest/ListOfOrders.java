package ReturnListOfOrdersTest;

import createCourierTests.CourierCreds;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.notNullValue;

public class ListOfOrders {
    CourierCreds courierCreds;
    Integer courierId;

    @BeforeEach
    public void init(){
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        // Создание курьера
        courierCreds = new CourierCreds(RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20));
        courierId = null;
        RestAssured.given()
                .header("Content-type", "application/json")
                .and()
                .body(courierCreds)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);

        // Логин под курьером для получения айди
        String login = courierCreds.getLogin();
        String password = courierCreds.getPassword();
        HashMap<String, String> logoPass = new HashMap<>();
        logoPass.put("login", login);
        logoPass.put("password", password);

        Response response = RestAssured.given()
                .header("Content-type", "application/json")
                .body(logoPass)
                .when()
                .post("/api/v1/courier/login");
        response.then().statusCode(200).and().body("id", notNullValue());
        courierId = response.then().extract().path("id");
    }

    @Test
    @DisplayName("Проверка возврата списка заказов")
    public void getListOfOrders(){
        RestAssured.given()
                .queryParam("courierId", courierId)
                .when()
                .get("/api/v1/orders")
                .then()
                .assertThat()
                .body("orders", notNullValue());
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
