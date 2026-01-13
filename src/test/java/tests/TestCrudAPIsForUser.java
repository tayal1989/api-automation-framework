package tests;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TestCrudAPIsForUser {

    private static String userId;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://localhost:3000";
    }

    // 1️⃣ POST → Create user
    @Test(priority = 1)
    public void createUser() {

        String userData = """
        {
          "name": "Myra Agarwal",
          "email": "myra@gmail.com",
          "roles": ["USER"],
          "preferences": {
            "language": "en",
            "notifications": ["PUSH"]
          },
          "active": true
        }
        """;

        userId =
                    given()
                            .header("Content-Type", "application/json")
                            .body(userData)
                            .when()
                            .post("/users")
                            .then()
                            .statusCode(201)
                            .extract()
                            .path("id").toString();

        System.out.println("Created user with id: " + userId);
    }

    // 2️⃣ GET → Validate creation
    @Test(priority = 2)
    public void validateUserCreated() {

        given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Myra Agarwal"))
                .body("active", equalTo(true));
    }

    // 3️⃣ PATCH → Partial update (active = false)
    @Test(priority = 3)
    public void deactivateUser() {

        String patchBody = """
        {
          "active": false
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(patchBody)
                .when()
                .patch("/users/" + userId)
                .then()
                .statusCode(200)
                .body("active", equalTo(false));
    }

    // 4️⃣ PUT → Full update
    @Test(priority = 4)
    public void updateUserWithPut() {

        String putBody = """
        {
          "id": "%s",
          "name": "Myra Agarwal",
          "email": "myra.updated@gmail.com",
          "roles": ["USER", "EDITOR"],
          "preferences": {
            "language": "en",
            "notifications": ["EMAIL"]
          },
          "active": false
        }
        """.formatted(userId);

        given()
                .header("Content-Type", "application/json")
                .body(putBody)
                .when()
                .put("/users/" + userId)
                .then()
                .statusCode(200)
                .body("email", equalTo("myra.updated@gmail.com"))
                .body("roles", hasItem("EDITOR"));
    }

    // 5️⃣ DELETE → Cleanup
    @Test(priority = 5)
    public void deleteUser() {

        given()
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(200);
    }

    // 6️⃣ GET → Validate deletion
    @Test(priority = 6)
    public void validateUserDeleted() {

        given()
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(404);
    }
}
