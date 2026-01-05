package tests.GET;

import base.BaseTest;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PingTest extends BaseTest {

    @Test
    public void testPing() {
//        RestAssured.baseURI = "http://localhost:3000";

        given()
                .when()
                    .get("/health")
                .then()
                    .log().all()
                    .statusCode(200)
                    .body("ping", equalTo("pong"));
    }
}

