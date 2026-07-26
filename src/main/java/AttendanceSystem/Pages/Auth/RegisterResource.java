package AttendanceSystem.Pages.Auth;

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

@Path("/register")
public class RegisterResource {

    @Inject
    Template register;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showRegister() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Register");
        data.put("subtitle", "Create your account");
        data.put("version", "1.0.0");
        data.put("username", "");
        data.put("email", "");
        data.put("error", null);
        data.put("success", null);
        data.put("message", null); 
        data.put("type", null); 

        String html = register.data(data).render();
        return Response.ok(html).build();
    }
}