package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class CardDBSourceTest {

    @Test
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

    @Test
    public void testUserCountEndpoint() {
        given()
                .when()
                .get("/api/admin/users/count")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data.totalUsers", notNullValue());
    }
}