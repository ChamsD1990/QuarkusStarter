package AttendanceSystem.Pages.Dashboards;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/dashboard")
public class Dashboard {

    @Inject
    Template dashboard;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard() {
        String starthours = "08";
        String startMinute = "00";

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Dashboard");
        data.put("username", "Admin User");
        data.put("lastLogin", "Today at 08:30 AM");
        data.put("totalEmployees", 150);
        data.put("presentToday", 120);
        data.put("absentToday", 30);
        data.put("attendanceRate", 80);
        data.put("recentActivities", getRecentActivities());
        data.put("currentYear", LocalDateTime.now().getYear());
        data.put("version", "1.0.0");
        data.put("startHours", starthours);
        data.put("startMinute", startMinute);
        data.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        TemplateInstance template = dashboard.data(data);
        String html = template.render();
        return Response.ok(html).build();
    }

    private List<Activity> getRecentActivities() {
        return Arrays.asList(
                new Activity("login", "John Doe", "08:15 AM"),
                new Activity("logout", "Jane Smith", "05:30 PM"),
                new Activity("login", "Bob Johnson", "09:00 AM"),
                new Activity("attendance", "Alice Brown", "08:45 AM"),
                new Activity("login", "Charlie Wilson", "07:50 AM"));
    }

    public static class Activity {
        public String type;
        public String action;
        public String user;
        public String time;

        public Activity(String type, String user, String time) {
            this.type = type;
            this.action = type.substring(0, 1).toUpperCase() + type.substring(1);
            this.user = user;
            this.time = time;
        }
    }
}