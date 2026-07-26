package AttendanceSystem.Pages;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/login")
public class LoginResource {

    @Inject
    Template login; // Matches login.html in templates folder

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showLogin() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Login");
        data.put("subtitle", "Enter your credentials to access the system");
        data.put("version", "1.0.0");
        data.put("username", "");
        data.put("error", null);
        data.put("success", null);

        TemplateInstance template = login.data(data);
        return Response.ok(template).build();
    }

    // Optional: Show login with error message
    public Response showLoginWithError(String error) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Login");
        data.put("subtitle", "Enter your credentials to access the system");
        data.put("version", "1.0.0");
        data.put("username", "");
        data.put("error", error);
        data.put("success", null);

        TemplateInstance template = login.data(data);
        return Response.ok(template).build();
    }
}