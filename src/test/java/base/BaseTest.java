package base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite
    public void setup() {
        RestAssured.baseURI = "http://localhost:3000";

        RestAssured.filters(new AllureRestAssured());
    }
}
