package AttendanceSystem;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/attendance")
public class AttendanceSource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAttendance() {
        return Response.ok("{\"status\":\"ok\"}").build();
    }

    @Test
    @Order(0)
    public void testAttendanceEndpoint() {
        given()
                .when()
                .get("/attendance")
                .then()
                .statusCode(200)
                .body("status", is("ok"));
    }
}