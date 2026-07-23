package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class CardDBSourceTest {

    @Test
    public void testHealthEndpoint() {
        given()
                .when().get("/health")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data", notNullValue())
                .body("data.status", is("UP"));
    }

    @Test
    public void testApiEndpoint() {
        given()
                .when().get("/api")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("data", notNullValue())
                .body("data.name", is("Attendance System API"));
    }

    @Test
    @Disabled("Skip until CardService is properly implemented")
    public void testCardEndpoint() {
        given()
                .when().get("/cards")
                .then()
                .statusCode(200);
    }

    @Test
    public void testRootEndpoint() {
        given()
                .when().get("/")
                .then()
                .statusCode(200);
    }
}