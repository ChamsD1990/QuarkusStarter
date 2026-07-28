package AttendanceSystem.Pages.Auth;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import AttendanceSystem.Service.SessionService;

@Path("/login")
public class LoginResource {

    @Inject
    Template login;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showLogin() {
        // Check login session
        Boolean isSession = SessionService.runSession();

        if (isSession) {
            System.out.println("Session valid, redirecting to dashboard...");
            return Response.seeOther(UriBuilder.fromPath("/dashboard").build()).build();
        }

        // Jika session tidak valid, tampilkan halaman login
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Login");
        data.put("subtitle", "Enter your credentials");
        data.put("version", "1.0.0");
        data.put("username", "");
        data.put("error", null);
        data.put("success", null);

        TemplateInstance template = login.data(data);
        String html = template.render();
        return Response.ok(html).build();
    }
}