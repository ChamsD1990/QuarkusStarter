package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class CardDBSourceTest {

    @Test
    public void testHealthEndpoint() {
        given()
                .when().get("/health")
                .then()
                .statusCode(200)
                .body("status", is("success"));
    }

    @Test
    public void testApiEndpoint() {
        given()
                .when().get("/api")
                .then()
                .statusCode(200)
                .body("status", is("success"));
    }

    @Test
    public void testRootEndpoint() {
        given()
                .when().get("/")
                .then()
                .statusCode(200);
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
    @Disabled("Skip until CardService is properly implemented")
    public void testGetCardById() {
        given()
                .when().get("/cards/1")
                .then()
                .statusCode(200);
    }
}