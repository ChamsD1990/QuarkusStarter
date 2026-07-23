package AttendanceSystem;

import AttendanceSystem.Model.*;
import AttendanceSystem.Service.helper.DedicatedIP;
import AttendanceSystem.Service.helper.JwtService;
import AttendanceSystem.Service.helper.RandomStringGenerator;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;


@Path("/")
public class MainSources {
    @Inject
    DedicatedIP dedicatedIP; 

    @Inject
    JwtService jwtService;
    
    private final HtmlReaderService htmlReaderService = new HtmlReaderService();

    @ConfigProperty(name = "quarkus.http.host", defaultValue = "0.0.0.0")
    String serverHost;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int serverPort;

    private String serverIP;

    // Session tracking untuk rate limiting
    private final Map<String, SessionData> sessions = new HashMap<>();
    private final Map<String, String> validCSRFTokens = new HashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    
    void onStart(@Observes StartupEvent event) {
        this.serverIP = dedicatedIP.getPublicIPv4();
        printBanner();
    }
    
    private void printBanner() {
        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════════════════╗\n" +
                "║                   🔒 SECURE ATTENDANCE SYSTEM                   ║\n" +
                "╠══════════════════════════════════════════════════════════════════╣\n" +
                "║  ✅ Server: http://" + serverIP + ":" + serverPort + "                                      ║\n" +
                "║  🔐 Security: JWT + Rate Limiting + IP Protection              ║\n" +
                "╚══════════════════════════════════════════════════════════════════╝\n");
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
    
    @POST
    @Path("/api/validate-session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateSession(
        @HeaderParam("Authorization") String auth,
        @HeaderParam("X-CSRF-Token") String csrfToken,
        Map<String, String> body,
        @Context HttpHeaders headers
    ) {
        String clientIp = dedicatedIP.getClientIp(headers);
        
        boolean valid = false;
        String message = "Invalid session";
         
        if (auth != null && auth.startsWith("Bearer ")) {
            String jwt = auth.substring(7);
            if (jwtService.validateToken(jwt, clientIp)) {
                valid = true;
                message = "Session validated";
            }
        }
         
        if (csrfToken != null && validCSRFTokens.containsValue(csrfToken)) {
            valid = true;
            message = "CSRF token validated";
        }
         
        String antiScrapeToken = body != null ? body.get("token") : null;
        if (antiScrapeToken != null && jwtService.validateScrapeToken(antiScrapeToken, clientIp)) {
            valid = true;
            message = "Anti-scrape token validated";
        }
        
        String response = String.format(
            "{\"valid\": %b, \"message\": \"%s\", \"ip\": \"%s\"}",
            valid, message, clientIp
        );
        
        return Response.ok(response)
            .header("Cache-Control", "no-cache")
            .build();
    }



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
        
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String jwtToken = jwtService.generateToken(sessionId, "user", clientIp);
        String csrfToken = jwtService.generateCsrfToken();
        String antiScrapeToken = jwtService.generateScrapeToken(clientIp);

        String html = htmlReaderService.readHtml();
        validCSRFTokens.put(sessionId, csrfToken);
        String secureHtml = html
                    .replace("<!-- SESSION_ID -->", sessionId)
                    .replace("<!-- JWT_TOKEN -->", jwtToken)
                    .replace("<!-- CSRF_TOKEN -->", csrfToken)
                    .replace("<!-- ANTI_SCRAPE_TOKEN -->", antiScrapeToken)
                    .replace("<!-- CLIENT_IP -->", clientIp)
                    .replace("<!-- TIMESTAMP -->", String.valueOf(Instant.now().getEpochSecond()));  return Response.ok(secureHtml)
                .cookie(new NewCookie("jwt_token", jwtToken, "/", null, null, 3600, true))
                .cookie(new NewCookie("session_id", sessionId, "/", null, null, 3600, true))
                .cookie(new NewCookie("csrf_token", csrfToken, "/", null, null, 3600, true))
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "DENY")
                .header("X-XSS-Protection", "1; mode=block")
                .header("Referrer-Policy", "no-referrer")
                .header("Content-Security-Policy",
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https:; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'")
                .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build();
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