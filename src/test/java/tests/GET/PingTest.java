package tests.GET;

import base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class PingTest extends BaseTest {

    @Test
    public void testPing() {
        given().
        when()
              .get("/health").
        then()
              .log().all()
              .statusCode(200)
              .body("ping", equalTo("pong"));
    }

    @Test
    public void testStatusResponseWithJsonPath() {
        Response response = RestAssured.given().get("/health").andReturn();
        Assert.assertTrue(response.statusCode() == 200, "Response status code is incorrect");

        JsonPath jsonPath = response.jsonPath();
        String result = jsonPath.getString("status");
        System.out.println(result);
    }

    @Test
    public void testNotificationResponseWithJsonPath() {
        Response response = RestAssured.given().get("/users").andReturn();
        Assert.assertTrue(response.statusCode() == 200, "Response status code is incorrect");

        JsonPath jsonPath = response.jsonPath();
        /*
        find {} → Groovy filter expression
        It scans the array
        Returns the first matching object
        Then you navigate inside it
         */
        String notifications = jsonPath.getString("find { it.name == 'Naina Agarwal' }.preferences.notifications[0]");
        System.out.println(notifications);
    }
}

