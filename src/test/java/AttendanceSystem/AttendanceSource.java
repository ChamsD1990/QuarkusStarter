package AttendanceSystem;

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
}