package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is; 

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CardDBSourceTest {

    
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

    
}