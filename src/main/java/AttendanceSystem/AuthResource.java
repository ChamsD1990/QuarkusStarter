package AttendanceSystem;

import AttendanceSystem.Model.ResultResponse;
import AttendanceSystem.Service.*;
import AttendanceSystem.Service.helper.JwtService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthDataSecure authService;

    @Inject
    JwtService jwtService; // Optional - if you want JWT tokens

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

        boolean authenticated = authService.authenticateUser(request.username, request.password);

        if (authenticated) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", request.username);
            data.put("loginTime", Instant.now().toString());

            // Optional: Generate JWT token
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

// Request DTOs
class RegistrationRequest {
    public String username;
    public String password;
}

class LoginRequest {
    public String username;
    public String password;
}

class ChangePasswordRequest {
    public String username;
    public String oldPassword;
    public String newPassword;
}