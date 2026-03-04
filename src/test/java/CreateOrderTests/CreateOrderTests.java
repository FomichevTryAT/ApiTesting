package CreateOrderTests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTests {
    private Integer trackId;

    @BeforeEach
    public void setConnection(){
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    public static ArrayList<File> orderData(){
        File fileColorBlack = new File("src/test/resources/WithColorBlack.json");
        File fileColorGrey = new File("src/test/resources/WithColorGrey.json");
        File fileTwoColor = new File("src/test/resources/WithTwoColors.json");

        return new ArrayList<>(){{
            add(fileColorBlack);
            add(fileColorGrey);
            add(fileTwoColor);
        }};
    }

    @ParameterizedTest
    @MethodSource("orderData")
    @DisplayName("Тест создания заказа с разными данными цветов самоката")
    public void createOrder(File jsonFile){
        Response response = RestAssured.given()
                .header("Content-type", "application/json")
                .body(jsonFile)
                .when()
                .post("/api/v1/orders");
        response.then().statusCode(201);
        trackId = response.then().extract().path("track");
    }

    @Test
    @DisplayName("Тело ответа содержит track")
    public void trackFieldIs(){
        ArrayList<String> color = new ArrayList<>();
        color.add("black");
        CreateOrderData orderData = new CreateOrderData(
                "firstName",
                "lastName",
                "keke",
                "koko",
                "+7123",
                2,
                "2020-06-06",
                "coco",
                color
        );

        Response response = RestAssured.given()
                .header("Content-type", "application/json")
                .body(orderData)
                .when()
                .post("/api/v1/orders");
        response.then().statusCode(201).and().body("track", notNullValue());
        trackId = response.then().extract().path("track");
    }

    @AfterEach
    public void cancelOrder(){
        HashMap<String, Integer> dataForDelete = new HashMap<>();
        dataForDelete.put("track", trackId);
        Response response = RestAssured.given()
                .header("Content-type", "application/json")
                .body(dataForDelete)
                .when()
                .put("/api/v1/orders/cancel");
    }
}
