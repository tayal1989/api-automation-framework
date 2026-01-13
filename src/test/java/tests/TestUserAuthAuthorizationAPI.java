package tests;

import base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TestUserAuthAuthorizationAPI extends BaseTest {

    private String userToken;

    // 1️⃣ Authentication (Login simulation)
    @Test(priority = 1)
    public void authenticateUsers() {

        String userId =
                given()
                        .queryParam("username", "naina")
                        .when()
                        .get("/authenticateUser")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("[0].id");

        // Simulate login for USER
        userToken =
                given()
                        .queryParam("userId", userId)
                        .when()
                        .get("/tokens")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("[0].token");

        // Simulate login for ADMIN
        String adminToken = given()
                .queryParam("role", "ADMIN")
                .when()
                .get("/tokens")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].token");

        System.out.println("User Token: " + userToken);
        System.out.println("Admin Token: " + adminToken);
    }

    // 2️⃣ Authenticated API (USER allowed)
    @Test(priority = 2)
    public void getUserProfile_withValidToken_shouldSucceed() {

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/authenticateUser/u-1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Naina Agarwal"));
    }

}
