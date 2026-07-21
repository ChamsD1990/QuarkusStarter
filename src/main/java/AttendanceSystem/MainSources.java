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
    @Produces(MediaType.APPLICATION_JSON)
    public Response root() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Attendance System API");
        response.put("status", "running");
        response.put("endpoints", getEndpoints());

        return Response.ok(ResultResponse.success(response)).build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Attendance System");
        health.put("timestamp", java.time.LocalDateTime.now().toString());

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

        return Response.ok(ResultResponse.success(apiInfo)).build();
    }

    private Map<String, String> getEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /", "Welcome page");
        endpoints.put("GET /health", "Health check");
        endpoints.put("GET /api", "API information");
        endpoints.put("GET /cards", "Get all cards");
        endpoints.put("GET /cards/{id}", "Get card by ID");
        endpoints.put("GET /cards/search?cardNo={cardNo}", "Search card by CardNo");
        endpoints.put("POST /cards", "Create new card");
        endpoints.put("PUT /cards/{id}", "Update card");
        endpoints.put("DELETE /cards/{id}", "Delete card");
        endpoints.put("POST /cards/{id}/upload", "Upload image for card");
        endpoints.put("GET /cards/{id}/image", "Get card image");
        return endpoints;
    }

    //
}