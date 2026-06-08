import createCourierTests.CourierCreds;
import io.restassured.RestAssured;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;

@DisplayName("Проверка удаления курьера")
public class DeleteCourierTest {
    private CourierCreds courierCreds;
    private Integer courierId;

    @BeforeEach
    public void init(){
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        courierCreds = new CourierCreds(RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20),
                RandomStringGenerator.builder().withinRange('a', 'z').get().generate(20));

        // Создаём курьера
        RestAssured.given()
                .header("Content-type", "application/json")
                .body(courierCreds)
                .when()
                .post("/api/v1/courier")
                .then()
                .assertThat().statusCode(201);

        // Сохраняем креды
        HashMap<String, String> logoPass = new HashMap<>();
        logoPass.put("login", courierCreds.getLogin());
        logoPass.put("password", courierCreds.getPassword());

        // Логинимся для получения id
        courierId = RestAssured.given()
                .header("Content-type", "application/json")
                .body(logoPass)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .assertThat().statusCode(200).and().extract().path("id");
    }

    @Test
    @DisplayName("Успешный запрос возвращает ok:true")
    public void courierDelete(){
        RestAssured.given()
                .contentType("application/json")
                .queryParam("id", courierId)
                .when()
                .delete("/api/v1/courier/")
                .then()
                .assertThat()
                .statusCode(200).and().body("ok", equalTo(true));
//        RestAssured.given()
//                .when()
//                .delete(String.format("/api/v1/courier/%d", courierId))
//                .then()
//                .assertThat()
//                .statusCode(200).and().body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Неуспешный запрос возвращает соответствующую ошибку. Тест запрос без id")
    public void deleteWithoutId(){
        RestAssured.given()
                .contentType("application/json")
                .queryParam("id", "")
                .when()
                .log().all()
                .delete("/api/v1/courier/?id=")
                .then()
                .assertThat().statusCode(400);
    }

    @Test
    @DisplayName("Неуспешный запрос возвращает соответствующую ошибку. Тест запроса с несуществующим id")
    public void deleteWithNotExistId(){
        RestAssured.given()
                .contentType("application/json")
                .when()
                .delete("/api/v1/courier/123")
                .then()
                .assertThat().statusCode(404);
    }
}
