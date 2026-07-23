package AttendanceSystem;

import io.quarkus.qute.Qute;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/dashboard")
public class Dashboard {

    // ============ WEB ENDPOINT (HTML) ============
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getDashboardWeb() {
        return Qute.fmt(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>{title}</title>
                            <style>
                                body { font-family: Arial; margin: 40px; }
                                .card {
                                    display: inline-block;
                                    padding: 20px;
                                    margin: 10px;
                                    border: 1px solid #ddd;
                                    border-radius: 8px;
                                    min-width: 150px;
                                }
                                .number { font-size: 24px; font-weight: bold; }
                            </style>
                        </head>
                        <body>
                            <h1>{title}</h1>
                            <div>
                                <div class="card">
                                    <div class="number">{totalEmployees}</div>
                                    <div>Total Employees</div>
                                </div>
                                <div class="card">
                                    <div class="number">{presentToday}</div>
                                    <div>Present Today</div>
                                </div>
                                <div class="card">
                                    <div class="number">{absentToday}</div>
                                    <div>Absent Today</div>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                Map.of(
                        "title", "Dashboard",
                        "totalEmployees", 150,
                        "presentToday", 120,
                        "absentToday", 30));
    }

    // ============ API ENDPOINT (JSON) ============
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardApi() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Dashboard");
        data.put("totalEmployees", 150);
        data.put("presentToday", 120);
        data.put("absentToday", 30);
        data.put("status", "success");
        data.put("timestamp", System.currentTimeMillis());

        return Response.ok(data).build();
    }
}