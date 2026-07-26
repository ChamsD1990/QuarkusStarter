package AttendanceSystem;

import AttendanceSystem.Model.*;
import AttendanceSystem.Service.helper.*;
import io.quarkus.runtime.StartupEvent;
import AttendanceSystem.Service.*;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONObject;

@Path("/")
public class MainSources {
    @Inject
    DedicatedIP dedicatedIP;

    @Inject
    JwtService jwtService;


    @ConfigProperty(name = "quarkus.http.host", defaultValue = "0.0.0.0")
    String serverHost;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int serverPort;
    public Boolean hasActiveSession = false;
    private String serverIP;
    public String currentSessionId;
    private final Map<String, SessionData> sessions = new HashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    private String cachedSessionJson = null;

    void onStart(@Observes StartupEvent event) {
        this.serverIP = dedicatedIP.getPublicIPv4();
        printBanner();
        // SQLiteHelper.main(null);
        checkExistingSession();
    }

    private void printBanner() {
        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════════════════ ╗\n" +
                "║                   🔒 SECURE ATTENDANCE SYSTEM                    ║\n" +
                "╠══════════════════════════════════════════════════════════════════ ╣\n" +
                "║  ✅ Server: http://" + serverIP + ":" + serverPort + "           ║\n" +
                "║  🔐 Security: JWT + Rate Limiting + IP Protection                ║\n" +
                "╚══════════════════════════════════════════════════════════════════╝\n");
    }

    private void checkExistingSession() {
        try {
            Connection conn = SQLiteHelper.getConnection();

            if (conn == null) {
                System.err.println("❌ Database connection failed!");
                hasActiveSession = false;
                cachedSessionJson = null;
                return;
            }

            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM sessionData ORDER BY session_created DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);

                    if (value instanceof java.sql.Timestamp) {
                        jsonObject.put(columnName, value.toString());
                    } else if (value instanceof java.sql.Date) {
                        jsonObject.put(columnName, value.toString());
                    } else {
                        jsonObject.put(columnName, value);
                    }
                }

                cachedSessionJson = jsonObject.toString();
                hasActiveSession = true;
                System.out.println("✅ Session data loaded successfully");

            } else {
                hasActiveSession = false;
                cachedSessionJson = null;
                System.out.println("ℹ️ No existing session found");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("❌ Error checking session!");
            e.printStackTrace();
            hasActiveSession = false;
            cachedSessionJson = null;
        }
    }

    private NewCookie createSessionCookie() {
        if (cachedSessionJson == null) {
            return null;
        }

        return new NewCookie.Builder("sessionData")
                .value(cachedSessionJson)
                .maxAge(24 * 60 * 60)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .build();
    }

    private boolean isRateLimited(String ip) {
        String key = ip;
        SessionData data = sessions.get(key);
        long now = System.currentTimeMillis();

        if (data == null || (now - data.getLastRequest()) > 60000) {
            sessions.put(key, new SessionData(1, now));
            return false;
        }

        if (data.getRequestCount() >= MAX_REQUESTS_PER_MINUTE) {
            return true;
        }

        data.setRequestCount(data.getRequestCount() + 1);
        data.setLastRequest(now);
        return false;
    }

    // Root endpoint - handles HTML requests
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRoot(@Context HttpHeaders headers) {
        String clientIp = dedicatedIP.getClientIp(headers);
        System.out.println("📡 Client IP: " + clientIp);

        if (dedicatedIP.isBlocked(clientIp)) {
            return Response.status(403)
                    .entity("<h1>⛔ Access Denied - Your IP is Blocked</h1>")
                    .build();
        }

        if (isRateLimited(clientIp)) {
            return Response.status(429)
                    .entity("<h1>⏳ Too Many Requests - Please wait 60 seconds</h1>")
                    .header("Retry-After", "60")
                    .build();
        }

        if (!hasActiveSession) {
            return Response.seeOther(URI.create("/login"))
                    .cookie(
                            new NewCookie("redirect_reason", "session_expired", "/", null, null, 60, false))
                    .build();
        } else {
            Response.ResponseBuilder responseBuilder = Response.seeOther(URI.create("/dashboard"))
                    .cookie(
                            new NewCookie("redirect_reason", "session_active", "/", null, null, 60, true));

            NewCookie sessionCookie = createSessionCookie();
            if (sessionCookie != null) {
                responseBuilder.cookie(sessionCookie);
                System.out.println("🍪 Session cookie added to response");
            }

            return responseBuilder.build();
        }
    }

    // Health endpoint - always returns JSON
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Attendance System");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        health.put("hasActiveSession", hasActiveSession);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", health);
        response.put("message", null);

        return Response.ok(response).build();
    }

    // API info endpoint - always returns JSON
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response apiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "Attendance System API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("endpoints", getEndpoints());
        apiInfo.put("hasActiveSession", hasActiveSession);
        apiInfo.put("timestamp", java.time.LocalDateTime.now().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", apiInfo);
        response.put("message", null);

        return Response.ok(response).build();
    }

    // Debug endpoint
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
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