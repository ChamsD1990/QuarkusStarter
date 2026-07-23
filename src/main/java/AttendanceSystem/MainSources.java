package AttendanceSystem;

import AttendanceSystem.Model.ResultResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class MainSources {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRoot() {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Attendance System</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; }
                        .container { max-width: 800px; margin: 0 auto; }
                        h1 { color: #333; }
                        .card {
                            padding: 20px;
                            margin: 10px 0;
                            border: 1px solid #ddd;
                            border-radius: 8px;
                            background: #f9f9f9;
                        }
                        .endpoint {
                            font-family: monospace;
                            background: #eee;
                            padding: 10px;
                            border-radius: 4px;
                            margin: 5px 0;
                        }
                        a { color: #0066cc; text-decoration: none; }
                        a:hover { text-decoration: underline; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🚀 Attendance System API</h1>
                        <p>Welcome to the Attendance System API. Here are the available endpoints:</p>

                        <div class="card">
                            <h3>📊 System Endpoints</h3>
                            <div class="endpoint">GET <a href="/health">/health</a> - Health check</div>
                            <div class="endpoint">GET <a href="/api">/api</a> - API information</div>
                            <div class="endpoint">GET <a href="/dashboard">/dashboard</a> - Dashboard (HTML)</div>
                            <div class="endpoint">GET <a href="/dashboard/api">/dashboard/api</a> - Dashboard API (JSON)</div>
                        </div>

                        <div class="card">
                            <h3>💳 Card Endpoints</h3>
                            <div class="endpoint">GET <a href="/cards">/cards</a> - Get all cards</div>
                            <div class="endpoint">GET /cards/{id} - Get card by ID</div>
                            <div class="endpoint">GET /cards/search?cardNo={cardNo} - Search cards</div>
                            <div class="endpoint">POST /cards - Create new card</div>
                            <div class="endpoint">PUT /cards/{id} - Update card</div>
                            <div class="endpoint">DELETE /cards/{id} - Delete card</div>
                        </div>

                        <p style="margin-top: 20px; color: #666; font-size: 12px;">
                            API Version: 1.0.0 | Built with Quarkus
                        </p>
                    </div>
                </body>
                </html>
                """;

        return Response.ok(html).build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Attendance System");
        health.put("timestamp", java.time.LocalDateTime.now().toString());

        // PASTIKAN menggunakan ResultResponse.success()
        return Response.ok(ResultResponse.success(health)).build();
    }

    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response apiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "Attendance System API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("endpoints", getEndpoints());

        // PASTIKAN menggunakan ResultResponse.success()
        return Response.ok(ResultResponse.success(apiInfo)).build();
    }

    private Map<String, String> getEndpoints() {
        Map<String, String> endpoints = new HashMap<>();

        endpoints.put("GET /health", "Health check");
        endpoints.put("GET /api", "API information");
        endpoints.put("GET /dashboard", "Dashboard page (HTML)");
        endpoints.put("GET /dashboard/api", "Dashboard data (JSON)");
        endpoints.put("GET /cards", "Get all cards");
        endpoints.put("GET /cards/{id}", "Get card by ID");
        endpoints.put("GET /cards/search", "Search cards");
        endpoints.put("POST /cards", "Create new card");
        endpoints.put("PUT /cards/{id}", "Update card");
        endpoints.put("DELETE /cards/{id}", "Delete card");

        return endpoints;
    }
}