package tests;

import io.restassured.RestAssured;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class TestDeleteUser {
    @Test
    public void deleteUser() {

        RestAssured.baseURI = "http://localhost:3000";

        given()
                .when()
                .delete("/users/3")
                .then()
                .statusCode(200)
                .log().all();
    }

    @Test(dependsOnMethods = "deleteUser")
    public void validateUserDeleted() {

        RestAssured.baseURI = "http://localhost:3000";

        given()
                .when()
                .get("/users/3")
                .then()
                .statusCode(404);
    }

}
