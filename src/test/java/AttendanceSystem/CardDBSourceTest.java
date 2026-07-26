package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CardDBSourceTest {

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "Test@123456";

    // Clear database sebelum test
    @BeforeAll
    public static void setup() {
        given().when().post("/api/admin/clear").then().statusCode(200);
        System.out.println("✅ Database cleared");
    }

    @Test
    @Order(1)
    public void testHealthEndpoint() {
        given()
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .body("status", is("success"));
    }

    @Test
    @Order(2)
    public void testApiEndpoint() {
        given()
                .when()
                .get("/api")
                .then()
                .statusCode(200)
                .body("data.name", is("Attendance System API"));
    }

    @Test
    @Order(3)
    public void testRegisterUser() {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);

        given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/api/register")
                .then()
                .statusCode(201)
                .body("status", is("success"))
                .body("data.username", is(USERNAME));
    }

    @Test
    @Order(4)
    public void testLoginSuccess() {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", USERNAME, PASSWORD);

        given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/api/login")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data.token", notNullValue());
    }

    @Test
    @Order(5)
    public void testGetUserInfoSuccess() {
        given()
                .when()
                .get("/api/user/" + USERNAME)
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data.username", is(USERNAME));
    }

    @Test
    @Order(6)
    public void testUserCountEndpoint() {
        given()
                .when()
                .get("/api/admin/users/count")
                .then()
                .statusCode(200)
                .body("data.totalUsers", notNullValue());
    }

    @Test
    @Order(7)
    public void testLogout() {
        given()
                .when()
                .post("/api/logout")
                .then()
                .statusCode(200)
                .body("status", is("success"));
    }

    @Test
    @Order(8)
    public void testClearDatabase() {
        given()
                .when()
                .post("/api/admin/clear")
                .then()
                .statusCode(200)
                .body("status", is("success"));
    }
}