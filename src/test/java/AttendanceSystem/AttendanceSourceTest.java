package AttendanceSystem;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class AttendanceSourceTest {

    @Test
    public void testAttendanceEndpoint() {
        given()
                .when().get("/attendance") // Sesuaikan dengan @Path di AttendanceSource
                .then()
                .statusCode(200);
    }
}
