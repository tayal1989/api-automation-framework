package tests;

import base.BaseTest;
import constants.Endpoints;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.Test;
import models.Address;
import models.Student;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

@Epic("Student API")
@Feature("Student CRUD Operations")
public class TestStudentCRUD extends BaseTest {

    private static String studentId;

    // 1️⃣ POST → Serialize POJO → Create student
    @Test(priority = 1, description = "Create student using POST")
    @Story("Create Student")
    @Severity(SeverityLevel.CRITICAL)
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
                        .body(student) // 🔥 Serialization happens here
                .when()
                        .post(Endpoints.STUDENTS)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 201, "Expected status code 201 Created");
        assertTrue(response.getStatusLine().contains("Created"), "Status line should contain 'Created'");

        // Header assertions
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should contain application/json");
        assertTrue(response.getTime() < 5000, "Response time should be less than 5 seconds");

        // Body assertions
        studentId = response.jsonPath().getString("id");
        assertNotNull(studentId, "Student ID should not be null");
        assertEquals(response.jsonPath().getString("name"), "Vishal Agarwal", "Name should match");
        assertEquals(response.jsonPath().getInt("age"), 37, "Age should be 37");
        assertTrue(response.jsonPath().getBoolean("active"), "Student should be active");
        assertEquals(response.jsonPath().getString("address.city"), "Kolkata", "City should be Kolkata");
        assertEquals(response.jsonPath().getString("address.state"), "Bengal", "State should be Bengal");
        assertThat(response.jsonPath().getList("marks"), hasItems(88, 92, 90));

        System.out.println("Created student with id: " + studentId);
    }

    // 2️⃣ GET → Deserialize JSON → POJO
    @Test(priority = 2, description = "Fetch student and validate details")
    @Story("Get Student")
    @Severity(SeverityLevel.NORMAL)
    public void validateStudentCreated() {
        Response response =
                given()
                        .pathParam("id", studentId)
                .when()
                        .get(Endpoints.STUDENT_BY_ID)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 200, "Expected status code 200 OK");
        assertTrue(response.getStatusLine().contains("OK"), "Status line should contain 'OK'");

        // Header assertions
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should contain application/json");
        assertTrue(response.getTime() < 3000, "Response time should be less than 3 seconds");

        // Deserialize and validate
        Student student = response.as(Student.class); // 🔥 Deserialization
        assertNotNull(student, "Student object should not be null");

        // Student field assertions
        assertEquals(student.getId(), studentId, "Student ID should match");
        assertThat(student.getName(), equalTo("Vishal Agarwal"));
        assertThat(student.getAge(), equalTo(37));
        assertThat(student.isActive(), equalTo(true));
        assertThat(student.getMarks(), hasItems(88, 92));
        assertThat(student.getMarks().size(), equalTo(3));

        // Address assertions
        assertNotNull(student.getAddress(), "Address should not be null");
        assertThat(student.getAddress().getCity(), equalTo("Kolkata"));
        assertThat(student.getAddress().getState(), equalTo("Bengal"));

        System.out.println(response.asPrettyString());
    }

    // 3️⃣ PATCH → Partial update (active = false)
    @Test(priority = 3, description = "Deactivate student using PATCH")
    @Story("Update Student")
    @Severity(SeverityLevel.NORMAL)
    public void deactivateStudent() {

        String patchBody = """
        {
          "active": false
        }
        """;

        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .pathParam("id", studentId)
                        .body(patchBody)
                .when()
                        .patch(Endpoints.STUDENT_BY_ID)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 200, "Expected status code 200 OK");
        assertTrue(response.getStatusLine().contains("OK"), "Status line should contain 'OK'");

        // Header assertions
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should contain application/json");

        // Body assertions - verify only 'active' changed, other fields intact
        assertFalse(response.jsonPath().getBoolean("active"), "Student should be deactivated");
        assertEquals(response.jsonPath().getString("name"), "Vishal Agarwal", "Name should remain unchanged");
        assertEquals(response.jsonPath().getInt("age"), 37, "Age should remain unchanged");
        assertEquals(response.jsonPath().getString("id"), studentId, "ID should remain unchanged");
    }

    // 4️⃣ PUT → Full update using POJO
    @Test(priority = 4, description = "Update student with put request")
    @Story("Update Student")
    @Severity(SeverityLevel.CRITICAL)
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

        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .pathParam("id", studentId)
                        .body(updatedStudent)
                .when()
                        .put(Endpoints.STUDENT_BY_ID)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 200, "Expected status code 200 OK");
        assertTrue(response.getStatusLine().contains("OK"), "Status line should contain 'OK'");

        // Header assertions
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should contain application/json");
        assertTrue(response.getTime() < 3000, "Response time should be less than 3 seconds");

        // Body assertions - verify all fields updated
        assertEquals(response.jsonPath().getString("id"), studentId, "ID should match");
        assertEquals(response.jsonPath().getString("name"), "Vishal Ram Avtar Agarwal", "Name should be updated");
        assertEquals(response.jsonPath().getInt("age"), 37, "Age should be 37");
        assertFalse(response.jsonPath().getBoolean("active"), "Student should be inactive");
        assertEquals(response.jsonPath().getString("address.city"), "Mumbai", "City should be updated to Mumbai");
        assertEquals(response.jsonPath().getString("address.state"), "Maharashtra", "State should be updated to Maharashtra");
        assertThat(response.jsonPath().getList("marks"), hasItems(90, 91, 93));
    }

    // 5️⃣ DELETE → Cleanup
    @Test(priority = 5, description = "Delete student and validate removal")
    @Story("Delete Student")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteStudent() {

        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .pathParam("id", studentId)
                .when()
                        .delete(Endpoints.STUDENT_BY_ID)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 200, "Expected status code 200 OK for successful deletion");
        assertTrue(response.getStatusLine().contains("OK"), "Status line should contain 'OK'");

        // Response time assertion
        assertTrue(response.getTime() < 3000, "Delete operation should complete within 3 seconds");

        System.out.println("Student with id " + studentId + " deleted successfully");
    }

    // 6️⃣ GET → Validate deletion
    @Test(priority = 6, description = "Verify student no longer exists after deletion")
    @Story("Validate Delete Student")
    @Severity(SeverityLevel.CRITICAL)
    public void validateStudentDeleted() {

        Response response =
                given()
                        .header("Content-Type", "application/json")
                        .pathParam("id", studentId)
                .when()
                        .get(Endpoints.STUDENT_BY_ID)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 404, "Expected status code 404 Not Found");
        assertTrue(response.getStatusLine().contains("Not Found"), "Status line should contain 'Not Found'");

        // Response time assertion
        assertTrue(response.getTime() < 3000, "Response time should be less than 3 seconds");

        System.out.println("Verified: Student with id " + studentId + " no longer exists (404)");
    }

    // 7️⃣ GET → Get all students
    @Test(priority = 7, description = "Get all students data")
    @Story("Get All Students")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllStudents() {

        Response response =
                given()
                .when()
                        .get(Endpoints.STUDENTS)
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 200, "Expected status code 200 OK");
        assertTrue(response.getStatusLine().contains("OK"), "Status line should contain 'OK'");

        // Header assertions
        assertTrue(response.getContentType().contains("application/json"), "Content-Type should contain application/json");
        assertTrue(response.getTime() < 5000, "Response time should be less than 5 seconds");

        // Deserialize response
        List<Student> students = response.jsonPath().getList("", Student.class);

        // Collection assertions
        assertNotNull(students, "Students list should not be null");
        assertFalse(students.isEmpty(), "Students list should not be empty");
        assertEquals(students.size(), 2, "Should have exactly 2 students");

        // First student assertions
        assertNotNull(students.get(0), "First student should not be null");
        assertNotNull(students.get(0).getId(), "First student ID should not be null");

        // Second student assertions
        Student secondStudent = students.get(1);
        assertNotNull(secondStudent, "Second student should not be null");
        assertEquals(secondStudent.getName(), "Naina Agarwal", "Second student name should be Naina Agarwal");
        assertTrue(secondStudent.getMarks().contains(82), "Second student marks should contain 82");
        assertNotNull(secondStudent.getAddress(), "Second student address should not be null");
        assertEquals(secondStudent.getAddress().getState(), "Haryana", "Second student state should be Haryana");

        System.out.println("Total students: " + students.size());
        System.out.println("Student names: " + students.stream().map(Student::getName).toList());
    }

    @Test(priority = 8, description = "Get all students using proxy")
    @Story("Get All Students via Proxy")
    @Severity(SeverityLevel.MINOR)
    public void getAllStudentsUsingProxy() {

        // Skip this test - proxy not configured
        throw new SkipException("Skipping test: Proxy server not configured at 127.0.0.1:8888");

        /*
        Response response =
                given()
                        .proxy("127.0.0.1", 8888)
                .when()
                        .get("/students")
                .then()
                        .extract()
                        .response();

        // Status code assertions
        assertEquals(response.getStatusCode(), 200, "Expected status code 200 OK");
        assertTrue(response.getStatusLine().contains("OK"), "Status line should contain 'OK'");

        // Deserialize response
        List<Student> students = response.jsonPath().getList("", Student.class);

        // Collection assertions
        assertNotNull(students, "Students list should not be null");
        assertEquals(students.size(), 2, "Should have exactly 2 students");

        // Validate second student
        assertEquals(students.get(1).getName(), "Naina Agarwal");
        assertTrue(students.get(1).getMarks().contains(82));
        assertEquals(students.get(1).getAddress().getState(), "Haryana");

        System.out.println("Proxy test - Total students: " + students.size());
        */
    }
}
