package AttendanceSystem;

import AttendanceSystem.Model.*;
import AttendanceSystem.Model.Auth.*;
import AttendanceSystem.Service.*;
import AttendanceSystem.Service.helper.DedicatedIP;
import AttendanceSystem.Service.helper.JwtService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiSource {
    @Inject
    DedicatedIP dedicatedIP;

    @Inject
    AuthDataSecure authService;

    @Inject
    JwtService jwtService;
    private final Map<String, String> validCSRFTokens = new HashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "Attendance System API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("status", "running");
        apiInfo.put("timestamp", Instant.now().toString());

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /api/validate-session", "Validate session with JWT, CSRF, and anti-scrape tokens");
        endpoints.put("POST /api/register", "Register a new user");
        endpoints.put("POST /api/login", "Login user");
        endpoints.put("POST /api/logout", "Logout user");
        endpoints.put("PUT /api/change-password", "Change user password");
        endpoints.put("GET /api/user/{username}", "Get user information");
        endpoints.put("POST /api/admin/unlock/{username}", "Unlock user account (admin)");
        endpoints.put("DELETE /api/admin/user/{username}", "Delete user (admin)");
        endpoints.put("GET /api/admin/users/count", "Get total user count (admin)");
        endpoints.put("POST /api/admin/clear", "Clear database (admin)");
        apiInfo.put("endpoints", endpoints);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", apiInfo);
        response.put("message", null);

        return Response.ok(response).build();
    }

    @POST
    @Path("/validate-session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateSession(
            @HeaderParam("Authorization") String auth,
            @HeaderParam("X-CSRF-Token") String csrfToken,
            Map<String, String> body,
            @Context HttpHeaders headers) {
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
                valid, message, clientIp);

        return Response.ok(response)
                .header("Cache-Control", "no-cache")
                .build();
    }

    @POST
    @Path("/register")
    public Response register(RegistrationRequest request) {
        // Validate input
        if (request.username == null || request.username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Username cannot be empty"))
                    .build();
        }

        if (request.password == null || request.password.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Password cannot be empty"))
                    .build();
        }

        boolean registered = authService.registerUser(request.username, request.password);

        if (registered) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", request.username);
            data.put("registeredAt", Instant.now().toString());
            return Response.ok(ResultResponse.success("User registered successfully")).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse
                            .error("Registration failed. Username may already exist or password is too weak"))
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        if (request.username == null || request.username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Username cannot be empty"))
                    .build();
        }

        if (request.password == null || request.password.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Password cannot be empty"))
                    .build();
        }

        boolean authenticated = authService.authenticateUser(request.username, request.password);

        if (authenticated) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", request.username);
            data.put("loginTime", Instant.now().toString());

            // String token = jwtService.generateToken(request.username);
            // data.put("token", token);

            return Response.ok(ResultResponse.success("Login successful")).build();
        } else {
            // Don't reveal if user exists or password is wrong (security best practice)
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ResultResponse.error("Invalid credentials or account locked"))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        // In production, invalidate JWT token or session
        Map<String, String> data = new HashMap<>();
        data.put("logoutTime", Instant.now().toString());
        return Response.ok(ResultResponse.success("Logout successful")).build();
    }

    @PUT
    @Path("/change-password")
    public Response changePassword(ChangePasswordRequest request) {
        // Validate input
        if (request.username == null || request.username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Username cannot be empty"))
                    .build();
        }

        if (request.oldPassword == null || request.oldPassword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Old password cannot be empty"))
                    .build();
        }

        if (request.newPassword == null || request.newPassword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("New password cannot be empty"))
                    .build();
        }

        boolean changed = authService.changePassword(
                request.username,
                request.oldPassword,
                request.newPassword);

        if (changed) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", request.username);
            data.put("changedAt", Instant.now().toString());
            return Response.ok(ResultResponse.success("Password changed successfully")).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse
                            .error("Password change failed. Check your old password or try a stronger new password"))
                    .build();
        }
    }

    @GET
    @Path("/user/{username}")
    public Response getUserInfo(@PathParam("username") String username) {
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResultResponse.error("Username cannot be empty"))
                    .build();
        }

        if (!authService.userExists(username)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResultResponse.error("User not found"))
                    .build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("createdAt", authService.getUserCreatedAt(username));
        data.put("exists", true);

        return Response.ok(ResultResponse.success("User found")).build();
    }

    // Admin endpoints
    @POST
    @Path("/admin/unlock/{username}")
    public Response unlockAccount(@PathParam("username") String username) {
        // In production, add role-based access control here
        boolean unlocked = authService.unlockAccount(username);

        if (unlocked) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("unlockedAt", Instant.now().toString());
            return Response.ok(ResultResponse.success("Account unlocked successfully")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResultResponse.error("User not found or account not locked"))
                    .build();
        }
    }

    @DELETE
    @Path("/admin/user/{username}")
    public Response deleteUser(@PathParam("username") String username) {
        // In production, add role-based access control here
        boolean deleted = authService.deleteUser(username);

        if (deleted) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("deletedAt", Instant.now().toString());
            return Response.ok(ResultResponse.success("data success")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResultResponse.error("User not found"))
                    .build();
        }
    }

    @GET
    @Path("/admin/users/count")
    public Response getUserCount() {
        int count = authService.getUserCount();
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", count);
        data.put("timestamp", Instant.now().toString());
        return Response.ok(ResultResponse.success(data)).build();
    }

    @POST
    @Path("/admin/clear")
    public Response clearDatabase() {
        authService.clearDatabase();
        Map<String, Object> data = new HashMap<>();
        data.put("clearedAt", Instant.now().toString());
        return Response.ok(ResultResponse.success("Database cleared successfully")).build();
    }
}