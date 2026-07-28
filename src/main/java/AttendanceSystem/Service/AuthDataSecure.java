package AttendanceSystem.Service;

import io.quarkus.arc.Unremovable;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import AttendanceSystem.Service.helper.SQLiteHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Unremovable
public class AuthDataSecure {

    private static final Logger LOG = Logger.getLogger(AuthDataSecure.class);

    @ConfigProperty(name = "auth.salt.length", defaultValue = "16")
    int saltLength;

    @ConfigProperty(name = "auth.max.failed.attempts", defaultValue = "5")
    int maxFailedAttempts;

    @ConfigProperty(name = "auth.lockout.duration.minutes", defaultValue = "15")
    int lockoutDurationMinutes;

    private final Map<String, FailedLoginAttempt> failedAttempts = new ConcurrentHashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();

    // ==================== INNER CLASSES ====================
    private static class FailedLoginAttempt {
        private int count;
        private Instant firstAttempt;

        public FailedLoginAttempt() {
            this.count = 1;
            this.firstAttempt = Instant.now();
        }

        public int getCount() {
            return count;
        }

        public void increment() {
            count++;
        }

        public Instant getFirstAttempt() {
            return firstAttempt;
        }
    }

    private static class UserCredential {
        private final String salt;
        private final String hashedPassword;
        private final Instant createdAt;
        private boolean isLocked;
        private Instant lockedUntil;

        public UserCredential(String salt, String hashedPassword, Instant createdAt,
                boolean isLocked, Instant lockedUntil) {
            this.salt = salt;
            this.hashedPassword = hashedPassword;
            this.createdAt = createdAt;
            this.isLocked = isLocked;
            this.lockedUntil = lockedUntil;
        }

        public String getSalt() {
            return salt;
        }

        public String getHashedPassword() {
            return hashedPassword;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public boolean isLocked() {
            return isLocked;
        }

        public Instant getLockedUntil() {
            return lockedUntil;
        }
    }

    public Response authenticateUser(String username, String password) {
        if (username == null || password == null) {
            LOG.warn("❌ Username or password is null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"Username and password are required\"}")
                    .build();
        }

        try {
            UserCredential user = getUserFromDatabase(username);

            if (user == null) {
                recordFailedAttempt(username);
                LOG.warnf("❌ User not found: %s", username);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"status\":\"error\",\"code\":404,\"message\":\"User not found\"}")
                        .build();
            }

            // Print user data
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("createdAt", user.getCreatedAt());
            json.put("isLocked", user.isLocked());
            json.put("salt", user.getSalt());
            System.out.println("🔍 User data from SQLite: " + json.toString(2)); 
            if (user.isLocked()) {
                if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
                    LOG.warnf("🔒 Account locked for user: %s until %s", username, user.getLockedUntil());
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"status\":\"error\",\"code\":403,\"message\":\"Account is locked until "
                                    + user.getLockedUntil() + "\"}")
                            .build();
                } else {
                    unlockAccount(username);
                    failedAttempts.remove(username);
                    LOG.infof("🔓 Account unlocked for user: %s", username);
                }
            }

            // Hash password
            String hashedInput = hashPassword(password, user.getSalt());
            boolean authenticated = hashedInput.equals(user.getHashedPassword());

            System.out.println("🔐 Password hash: " + hashedInput);
            System.out.println("🔐 Stored hash:  " + user.getHashedPassword());
            System.out.println("✅ Match: " + authenticated);

            if (authenticated) {
                failedAttempts.remove(username);
                Timestamp session_created = Timestamp.from(Instant.now());
                Timestamp session_max = Timestamp.from(Instant.now().plusSeconds(60)); 
                String sql = """
                        INSERT INTO sessionData (username, password, session_created, session_max, isAuthenticated)
                        VALUES (?, ?, ?, ?, ?)
                        """; 
                try (Connection conn = SQLiteHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { 
                    pstmt.setString(1, username);
                    pstmt.setString(2, hashedInput);
                    pstmt.setTimestamp(3, session_created);
                    pstmt.setTimestamp(4, session_max);
                    pstmt.setInt(5, authenticated ? 1 : 0); 
                    int rows = pstmt.executeUpdate();
                    LOG.infof("✅ Inserted %d row(s)", rows); 
                } catch (SQLException e) {
                    LOG.errorf("❌ Error: %s", e.getMessage());
                } 

                JSONObject successJson = new JSONObject();
                successJson.put("status", "success");
                successJson.put("code", 200);
                successJson.put("message", "Login successful");
                successJson.put("username", username);
                successJson.put("timestamp", Instant.now().toString());

                return Response.ok(successJson.toString()).build();
            } else {
                recordFailedAttempt(username);
                LOG.warnf("❌ Failed login attempt for user: %s", username);
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"status\":\"error\",\"code\":401,\"message\":\"Invalid credentials\"}")
                        .build();
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("❌ Error hashing password", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Internal server error\"}")
                    .build();
        } catch (SQLException e) {
            LOG.error("❌ Database error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }
 
    private boolean checkPassword(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        try {
            UserCredential user = getUserFromDatabase(username);
            if (user == null) {
                return false;
            }

            if (user.isLocked()) {
                if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
                    return false;
                } else {
                    unlockAccount(username);
                    failedAttempts.remove(username);
                }
            }

            String hashedInput = hashPassword(password, user.getSalt());
            return hashedInput.equals(user.getHashedPassword());

        } catch (NoSuchAlgorithmException | SQLException e) {
            LOG.error("❌ Error checking password", e);
            return false;
        }
    }
 
    private UserCredential getUserFromDatabase(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?"; 
        try (Connection conn = SQLiteHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery(); 
            if (rs.next()) {
                return new UserCredential(
                        rs.getString("salt"),
                        rs.getString("hashed_password"),
                        Instant.parse(rs.getString("created_at")),
                        rs.getInt("is_locked") == 1,
                        rs.getString("locked_until") != null ? Instant.parse(rs.getString("locked_until")) : null);
            }
        }
        return null;
    }
 
    public Response registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            LOG.warn("❌ Registration attempt with empty username");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"Username is required\"}")
                    .build();
        }

        if (!isPasswordStrong(password)) {
            LOG.warnf("❌ Weak password attempt for user: %s", username);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"Password must be at least 8 characters with uppercase, lowercase, digit and special character\"}")
                    .build();
        }

        if (userExists(username)) {
            LOG.warnf("❌ Registration attempt for existing user: %s", username);
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"status\":\"error\",\"code\":409,\"message\":\"Username already exists\"}")
                    .build();
        }

        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);

            String sql = """
                    INSERT INTO users (username, salt, hashed_password, created_at, is_locked)
                    VALUES (?, ?, ?, ?, 0)
                    """;

            try (Connection conn = SQLiteHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                pstmt.setString(2, salt);
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, Instant.now().toString());

                int affected = pstmt.executeUpdate();

                if (affected > 0) {
                    LOG.infof("✅ User registered successfully: %s", username);
                    printUserData(username);

                    JSONObject successJson = new JSONObject();
                    successJson.put("status", "success");
                    successJson.put("code", 201);
                    successJson.put("message", "User registered successfully");
                    successJson.put("username", username);
                    successJson.put("timestamp", Instant.now().toString());

                    return Response.status(Response.Status.CREATED)
                            .entity(successJson.toString())
                            .build();
                }
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Failed to register user\"}")
                        .build();
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("❌ Error hashing password", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Internal server error\"}")
                    .build();
        } catch (SQLException e) {
            LOG.error("❌ Database error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }

    public Response changePassword(String username, String oldPassword, String newPassword) {
        // Validasi input
        if (username == null || username.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"Username cannot be empty\"}")
                    .build();
        }

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"Old password cannot be empty\"}")
                    .build();
        }

        if (newPassword == null || newPassword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"New password cannot be empty\"}")
                    .build();
        }
 
        if (!checkPassword(username, oldPassword)) {
            LOG.warnf("❌ Password change failed - invalid old password for user: %s", username);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"error\",\"code\":401,\"message\":\"Invalid old password\"}")
                    .build();
        }
 
        if (!isPasswordStrong(newPassword)) {
            LOG.warnf("❌ Password change failed - weak new password for user: %s", username);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"New password is too weak. Must be at least 8 characters with uppercase, lowercase, digit and special character\"}")
                    .build();
        }

        // Cek new password sama dengan old password
        if (oldPassword.equals(newPassword)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"code\":400,\"message\":\"New password must be different from old password\"}")
                    .build();
        }

        try {
            String newSalt = generateSalt();
            String newHashedPassword = hashPassword(newPassword, newSalt);

            String sql = "UPDATE users SET salt = ?, hashed_password = ? WHERE username = ?";

            try (Connection conn = SQLiteHelper.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, newSalt);
                pstmt.setString(2, newHashedPassword);
                pstmt.setString(3, username);

                int affected = pstmt.executeUpdate();

                if (affected > 0) {
                    LOG.infof("✅ Password changed successfully for user: %s", username);

                    JSONObject json = new JSONObject();
                    json.put("status", "success");
                    json.put("code", 200);
                    json.put("message", "Password changed successfully");
                    json.put("username", username);
                    json.put("changedAt", Instant.now().toString());

                    return Response.ok(json.toString()).build();
                }

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Failed to change password\"}")
                        .build();
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("❌ Error hashing new password", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Internal server error\"}")
                    .build();
        } catch (SQLException e) {
            LOG.error("❌ Database error", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }

    public void unlockAccount(String username) {
        String sql = "UPDATE users SET is_locked = 0, locked_until = NULL WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();
            failedAttempts.remove(username);
            LOG.infof("🔓 Account unlocked for user: %s", username);

        } catch (SQLException e) {
            LOG.error("❌ Error unlocking account: " + username, e);
        }
    }

    private void recordFailedAttempt(String username) {
        FailedLoginAttempt attempt = failedAttempts.computeIfAbsent(username, k -> new FailedLoginAttempt());

        if (attempt.getFirstAttempt().plusSeconds(60 * 5).isBefore(Instant.now())) {
            failedAttempts.put(username, new FailedLoginAttempt());
            return;
        }

        attempt.increment();

        if (attempt.getCount() >= maxFailedAttempts) {
            lockAccount(username);
            LOG.warnf("🔒 Account locked for user: %s after %d failed attempts",
                    username, maxFailedAttempts);
        }
    }

    private void lockAccount(String username) {
        String sql = "UPDATE users SET is_locked = 1, locked_until = ? WHERE username = ?";
        Instant lockedUntil = Instant.now().plusSeconds(lockoutDurationMinutes * 60);

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lockedUntil.toString());
            pstmt.setString(2, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOG.error("❌ Error locking account: " + username, e);
        }
    }

    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            LOG.error("❌ Error checking user exists", e);
            return false;
        }
    }

    public Response getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;

            JSONObject json = new JSONObject();
            json.put("status", "success");
            json.put("code", 200);
            json.put("totalUsers", count);

            return Response.ok(json.toString()).build();

        } catch (SQLException e) {
            LOG.error("❌ Error getting user count", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }

    public Response deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                failedAttempts.remove(username);
                LOG.infof("🗑️ User deleted: %s", username);
                return Response.ok("{\"status\":\"success\",\"code\":200,\"message\":\"User deleted successfully\"}")
                        .build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"status\":\"error\",\"code\":404,\"message\":\"User not found\"}")
                    .build();

        } catch (SQLException e) {
            LOG.error("❌ Error deleting user", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }

    public Response getUserInfo(String username) {
        try {
            UserCredential user = getUserFromDatabase(username);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"status\":\"error\",\"code\":404,\"message\":\"User not found\"}")
                        .build();
            }

            JSONObject json = new JSONObject();
            json.put("status", "success");
            json.put("code", 200);
            json.put("username", username);
            json.put("createdAt", user.getCreatedAt());
            json.put("isLocked", user.isLocked());
            json.put("message", "User found");

            return Response.ok(json.toString()).build();

        } catch (SQLException e) {
            LOG.error("❌ Error getting user info", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }

    public Response clearDatabase() {
        String sql = "DELETE FROM users";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            failedAttempts.clear();
            LOG.info("🗑️ Database cleared");

            return Response.ok("{\"status\":\"success\",\"code\":200,\"message\":\"Database cleared successfully\"}")
                    .build();

        } catch (SQLException e) {
            LOG.error("❌ Error clearing database", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"code\":500,\"message\":\"Database error\"}")
                    .build();
        }
    }
 
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private String generateSalt() {
        byte[] saltBytes = new byte[saltLength];
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String saltedPassword = salt + password;
        byte[] hashedBytes = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
 
    private void printUserData(String username) {
        try {
            UserCredential user = getUserFromDatabase(username);
            if (user != null) {
                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("salt", user.getSalt());
                json.put("hashedPassword", user.getHashedPassword());
                json.put("createdAt", user.getCreatedAt());
                json.put("isLocked", user.isLocked());
                System.out.println("📝 User data: " + json.toString(2));
            }
        } catch (SQLException e) {
            LOG.error("Error printing user data", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        LOG.info("🔄 Shutting down AuthDataSecure...");
        SQLiteHelper.closeConnection();
        LOG.info("✅ Database connection closed.");
    }
}