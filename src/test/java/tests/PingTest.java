package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PingTest extends BaseTest {

    @Test
    public void testPing() {

        given()
                .when()
                .get("/health")
                .then()
                .log().all()
                .statusCode(200)
                .body("ping", equalTo("pong"));
    }
}

