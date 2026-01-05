package tests.PUT;

import io.restassured.RestAssured;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpdateUserWithPut {

    @Test
    public void updateUserWithPut() {

        RestAssured.baseURI = "http://localhost:3000";

        String putBody = """
                        {
                          "id": 3,
                          "name": "Myra Agarwal",
                          "email": "myra@gmail.com",
                          "roles": ["USER"],
                          "preferences": {
                            "language": "en",
                            "notifications": ["EMAIL"]
                          },
                          "active": false
                        }
                        """;

        given()
                .header("Content-Type", "application/json")
                .body(putBody).
        when()
                .put("/users/3").
        then()
                .statusCode(200)
                .body("active", equalTo(false))
                .log().all();
    }

}
