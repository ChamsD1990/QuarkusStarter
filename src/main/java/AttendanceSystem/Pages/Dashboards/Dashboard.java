package AttendanceSystem.Pages.Dashboards;
  
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

import AttendanceSystem.Service.HtmlReaderService;

@Path("/dashboard")
public class Dashboard {

    @Inject
    HtmlReaderService htmls;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard() {
        String html = htmls.readHtml("dashboard.html");
        return Response.ok(html).build();
    }

    private List<DashboardData.Activity> getRecentActivities() {
        return Arrays.asList(
                new DashboardData.Activity("login", "John Doe", "08:15 AM"),
                new DashboardData.Activity("logout", "Jane Smith", "05:30 PM"),
                new DashboardData.Activity("login", "Bob Johnson", "09:00 AM"),
                new DashboardData.Activity("attendance", "Alice Brown", "08:45 AM"),
                new DashboardData.Activity("login", "Charlie Wilson", "07:50 AM"));
    }
}