package tests.POST;

import base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateUserTest extends BaseTest {

    @Test
    public void testCreateAnotherUser() {
        String userData = """
                {
                  "id": 4,
                  "name": "Bimla Agarwal",
                  "email": "bimla.agarwal@gmail.com",
                  "roles": ["USER"],
                  "preferences": {
                    "language": "hi",
                    "notifications": ["PUSH"]
                  },
                  "active": false
                }
                """;

        given()
                .header("Content-Type", "application/json")
                .body(userData).
        when()
                .post("/users").
        then()
                .statusCode(201)
                .log().all()
                .assertThat()
                .body("id", is(notNullValue()));
    }
}

