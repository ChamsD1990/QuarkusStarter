package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CardDBSourceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Test@123456";
    private static final String NEW_PASSWORD = "NewPass@123";

    // ==================== BASIC TESTS ====================

    @Test
    @Order(1)
    public void testHealthEndpoint() {
        given()
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data.status", is("UP"))
                .body("data.service", is("Attendance System"));
    }

    @Test
    @Order(2)
    public void testApiEndpoint() {
        given()
                .when()
                .get("/api")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data.name", is("Attendance System API"))
                .body("data.version", is("1.0.0"))
                .body("data.status", is("running"))
                .body("data.endpoints", notNullValue())
                .body("data.endpoints.'POST /api/login'", is("Login user"))
                .body("data.endpoints.'POST /api/register'", is("Register a new user"));
    }

    @Test
    @Order(3)
    public void testApiInfoContainsAllEndpoints() {
        given()
                .when()
                .get("/api")
                .then()
                .statusCode(200)
                .body("data.endpoints.size()", is(10))
                .body("data.endpoints.'GET /api/user/{username}'", is("Get user information"))
                .body("data.endpoints.'POST /api/admin/clear'", is("Clear database (admin)"));
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @Order(4)
    public void testRegisterUser() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                TEST_USERNAME, TEST_PASSWORD);

        given()
                .contentType("application/json") // ← INI PENTING!
                .body(requestBody)
                .when()
                .post("/api/register")
                .then()
                .statusCode(201)
                .body("status", is("success"))
                .body("message", is("User registered successfully"))
                .body("data.username", is(TEST_USERNAME))
                .body("data.registeredAt", notNullValue());
    }

    @Test
    @Order(5)
    public void testRegisterDuplicateUser() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                TEST_USERNAME, TEST_PASSWORD);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/register")
                .then()
                .statusCode(409)
                .body("status", is("error"))
                .body("message", containsString("already exists"));
    }

    @Test
    @Order(6)
    public void testRegisterWeakPassword() {
        String requestBody = "{\"username\":\"weakuser\",\"password\":\"123\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/register")
                .then()
                .statusCode(400)
                .body("status", is("error"))
                .body("message", containsString("Password must be at least"));
    }

    @Test
    @Order(7)
    public void testRegisterEmptyUsername() {
        String requestBody = "{\"username\":\"\",\"password\":\"Test@123456\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/register")
                .then()
                .statusCode(400)
                .body("status", is("error"))
                .body("message", is("Username cannot be empty"));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @Order(8)
    public void testLoginSuccess() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                TEST_USERNAME, TEST_PASSWORD);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("message", is("Login successful"))
                .body("data.username", is(TEST_USERNAME))
                .body("data.token", notNullValue())
                .body("data.loginTime", notNullValue());
    }

    @Test
    @Order(9)
    public void testLoginWrongPassword() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"wrongpassword\"}",
                TEST_USERNAME);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/login")
                .then()
                .statusCode(401)
                .body("status", is("error"))
                .body("message", is("Invalid credentials"));
    }

    @Test
    @Order(10)
    public void testLoginUserNotFound() {
        String requestBody = "{\"username\":\"nonexistent\",\"password\":\"Test@123456\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/login")
                .then()
                .statusCode(404)
                .body("status", is("error"))
                .body("message", is("User not found"));
    }

    @Test
    @Order(11)
    public void testLoginEmptyUsername() {
        String requestBody = "{\"username\":\"\",\"password\":\"Test@123456\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/login")
                .then()
                .statusCode(400)
                .body("status", is("error"))
                .body("message", is("Username cannot be empty"));
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    @Order(12)
    public void testChangePasswordSuccess() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}",
                TEST_USERNAME, TEST_PASSWORD, NEW_PASSWORD);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/api/change-password")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("message", is("Password changed successfully"))
                .body("data.username", is(TEST_USERNAME))
                .body("data.changedAt", notNullValue());
    }

    @Test
    @Order(13)
    public void testChangePasswordWrongOldPassword() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"oldPassword\":\"wrongold\",\"newPassword\":\"%s\"}",
                TEST_USERNAME, NEW_PASSWORD);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/api/change-password")
                .then()
                .statusCode(401)
                .body("status", is("error"))
                .body("message", is("Invalid old password"));
    }

    @Test
    @Order(14)
    public void testChangePasswordSameAsOld() {
        String requestBody = String.format(
                "{\"username\":\"%s\",\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}",
                TEST_USERNAME, NEW_PASSWORD, NEW_PASSWORD);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/api/change-password")
                .then()
                .statusCode(400)
                .body("status", is("error"))
                .body("message", containsString("different from old password"));
    }

    // ==================== GET USER INFO TESTS ====================

    @Test
    @Order(15)
    public void testGetUserInfoSuccess() {
        given()
                .when()
                .get("/api/user/" + TEST_USERNAME)
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("message", is("User found"))
                .body("data.username", is(TEST_USERNAME))
                .body("data.createdAt", notNullValue())
                .body("data.isLocked", is(false));
    }

    @Test
    @Order(16)
    public void testGetUserInfoNotFound() {
        given()
                .when()
                .get("/api/user/nonexistent")
                .then()
                .statusCode(404)
                .body("status", is("error"))
                .body("message", is("User not found"));
    }

    // ==================== USER COUNT TESTS ====================

    @Test
    @Order(17)
    public void testUserCountEndpoint() {
        given()
                .when()
                .get("/api/admin/users/count")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("message", is("User count retrieved"))
                .body("data.totalUsers", notNullValue());
    }

    // ==================== CLEAR DATABASE TESTS ====================

    @Test
    @Order(18)
    public void testClearDatabase() {
        given()
                .when()
                .post("/api/admin/clear")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("message", is("Database cleared successfully"))
                .body("data.clearedAt", notNullValue());
    }

    @Test
    @Order(19)
    public void testUserCountAfterClear() {
        given()
                .when()
                .get("/api/admin/users/count")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data.totalUsers", is(0));
    }

    // ==================== LOGOUT TEST ====================

    @Test
    @Order(20)
    public void testLogout() {
        given()
                .when()
                .post("/api/logout")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("message", is("Logout successful"))
                .body("data.logoutTime", notNullValue());
    }
}