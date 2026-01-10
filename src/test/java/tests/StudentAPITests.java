package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import pojo.Address;
import pojo.Student;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class StudentAPITests extends BaseTest {

    private static String studentId;

    // 1️⃣ POST → Serialize POJO → Create student
    @Test(priority = 1)
    public void createStudent() {

        Address address = new Address();
        address.setCity("Kolkata");
        address.setState("Bengal");

        Student student = new Student();
        student.setId("vish2809");
        student.setName("Vishal Agarwal");
        student.setAge(37);
        student.setActive(true);
        student.setMarks(List.of(88, 92, 90));
        student.setAddress(address);

        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .body(student). // 🔥 Serialization happens here
                when()
                        .post("/students").
                then()
                        .statusCode(201)
                        .extract()
                        .response();

        studentId = response.jsonPath().getString("id");
        System.out.println("Created student with id: " + studentId);
    }

    // 2️⃣ GET → Deserialize JSON → POJO
    @Test(priority = 2)
    public void validateStudentCreated() {
        Response response =
                given().
                when()
                        .get("/students/" + studentId).
                then()
                        .statusCode(200)
                        .extract()
                        .response();

        Student student = response.as(Student.class); // 🔥 Deserialization

        assertThat(student.getName(), equalTo("Vishal Agarwal"));
        assertThat(student.getAge(), equalTo(37));
        assertThat(student.isActive(), equalTo(true));
        assertThat(student.getMarks(), hasItems(88, 92));
        assertThat(student.getAddress().getCity(), equalTo("Kolkata"));
    }

    // 3️⃣ PATCH → Partial update (active = false)
    @Test(priority = 3)
    public void deactivateStudent() {

        String patchBody = """
        {
          "active": false
        }
        """;

        given()
                .header("Content-Type", "application/json")
                .body(patchBody)
                .when()
                .patch("/students/" + studentId)
                .then()
                .statusCode(200)
                .body("active", equalTo(false));
    }

    // 4️⃣ PUT → Full update using POJO
    @Test(priority = 4)
    public void updateStudentWithPut() {

        Address address = new Address();
        address.setCity("Mumbai");
        address.setState("Maharashtra");

        Student updatedStudent = new Student();
        updatedStudent.setId(studentId);
        updatedStudent.setName("Vishal Ram Avtar Agarwal");
        updatedStudent.setAge(37);
        updatedStudent.setActive(false);
        updatedStudent.setMarks(List.of(90, 91, 93));
        updatedStudent.setAddress(address);

        given()
                .header("Content-Type", "application/json")
                .body(updatedStudent)
                .when()
                .put("/students/" + studentId)
                .then()
                .statusCode(200)
                .body("age", equalTo(37))
                .body("address.city", equalTo("Mumbai"));
    }

    // 5️⃣ DELETE → Cleanup
    @Test(priority = 5)
    public void deleteStudent() {

        given()
                .when()
                .delete("/students/" + studentId)
                .then()
                .statusCode(200);
    }

    // 6️⃣ GET → Validate deletion
    @Test(priority = 6)
    public void validateStudentDeleted() {

        given()
                .when()
                .get("/students/" + studentId)
                .then()
                .statusCode(404);
    }

    @Test(priority = 7)
    public void getAllStudents() {

        List<Student> students =
                given()
                        .when()
                        .get("/students")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .jsonPath()
                        .getList("", Student.class);

        System.out.println(students.size());
        System.out.println(students.get(1).getName());
        System.out.println(students.get(1).getMarks());
        System.out.println(students.get(1).getAddress().getState());

        assertEquals(students.size(), 2);
        assertEquals(students.get(1).getName(), "Naina Agarwal");
        assertTrue(students.get(1).getMarks().contains(82));
        assertEquals(students.get(1).getAddress().getState(), "Haryana");
    }

    @Test(priority = 8)
    public void getAllStudentsUsingProxy() {

        List<Student> students =
                given()
                        .proxy("127.0.0.1", 8888)
                        .when()
                        .get("/students")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .jsonPath()
                        .getList("", Student.class);

        System.out.println(students.size());
        System.out.println(students.get(1).getName());
        System.out.println(students.get(1).getMarks());
        System.out.println(students.get(1).getAddress().getState());

        assertEquals(students.size(), 2);
        assertEquals(students.get(1).getName(), "Naina Agarwal");
        assertTrue(students.get(1).getMarks().contains(82));
        assertEquals(students.get(1).getAddress().getState(), "Haryana");
    }
}
