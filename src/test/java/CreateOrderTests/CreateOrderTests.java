package CreateOrderTests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTests {
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
        response.then().statusCode(201).and().body("track", notNullValue());

        System.out.println(response.asPrettyString());
    }
}
