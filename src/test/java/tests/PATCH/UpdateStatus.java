package tests.PATCH;

import io.restassured.RestAssured;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UpdateStatus {

    @Test
    public void deactivateUser() {

        RestAssured.baseURI = "http://localhost:3000";

        String patchBody = """
                            {
                              "active": false
                            }
                            """;

        given()
                .header("Content-Type", "application/json")
                .body(patchBody).
        when()
                .patch("/users/3").
        then()
                .statusCode(200)
                .body("active", equalTo(false))
                .log().all();
    }

}
