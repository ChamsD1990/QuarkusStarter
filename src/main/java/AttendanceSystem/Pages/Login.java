package AttendanceSystem.Pages;
 
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response; 

import AttendanceSystem.Service.HtmlReaderService;

@Path("/login")
public class Login {


    @Inject
    HtmlReaderService htmls;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard() {
        String html = htmls.readHtml("login.html");
        return Response.ok(html).build();
    }
}

