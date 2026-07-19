package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class CardDBSourceTest {

    @Test
    public void testCardEndpoint() {
        given()
                .when().get("/card") // Sesuaikan dengan @Path di CardDBSource
                .then()
                .statusCode(200);
    }
}
