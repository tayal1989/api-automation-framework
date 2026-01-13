package base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;
import utils.ConfigReader;

public class BaseTest {

    @BeforeSuite
    public void setup() {
        RestAssured.baseURI = ConfigReader.getBaseUrl();

        RestAssured.filters(new AllureRestAssured());
    }
}
