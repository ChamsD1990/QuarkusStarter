package AttendanceSystem.Service;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import AttendanceSystem.Service.helper.SQLiteHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    // ==================== REGISTER USER ====================
    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            LOG.warn("❌ Registration attempt with empty username");
            return false;
        }

        if (!isPasswordStrong(password)) {
            LOG.warnf("❌ Weak password attempt for user: %s", username);
            return false;
        }

        if (userExists(username)) {
            LOG.warnf("❌ Registration attempt for existing user: %s", username);
            return false;
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
                    return true;
                }
                return false;
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("❌ Error hashing password", e);
            return false;
        } catch (SQLException e) {
            LOG.error("❌ Database error", e);
            return false;
        }
    }

    // ==================== AUTHENTICATE USER ====================
    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null) {
            LOG.warn("❌ Username or password is null");
            return false;
        }

        try {
            // Ambil user dari SQLite
            UserCredential user = getUserFromDatabase(username);

            if (user == null) {
                recordFailedAttempt(username);
                LOG.warnf("❌ User not found: %s", username);
                return false;
            }

            // Print user data
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("createdAt", user.getCreatedAt());
            json.put("isLocked", user.isLocked());
            json.put("salt", user.getSalt());
            System.out.println("🔍 User data from SQLite: " + json.toString(2));

            // Cek lock
            if (user.isLocked()) {
                if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
                    LOG.warnf("🔒 Account locked for user: %s until %s", username, user.getLockedUntil());
                    return false;
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
                LOG.infof("✅ User authenticated successfully: %s", username);
                return true;
            } else {
                recordFailedAttempt(username);
                LOG.warnf("❌ Failed login attempt for user: %s", username);
                return false;
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("❌ Error hashing password", e);
            return false;
        } catch (SQLException e) {
            LOG.error("❌ Database error", e);
            return false;
        }
    }

    // ==================== GET USER FROM SQLITE ====================
    private UserCredential getUserFromDatabase(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    // ==================== PRINT USER DATA ====================
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

    // ==================== RECORD FAILED ATTEMPT ====================
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

    // ==================== LOCK ACCOUNT ====================
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

    // ==================== UNLOCK ACCOUNT ====================
    public boolean unlockAccount(String username) {
        String sql = "UPDATE users SET is_locked = 0, locked_until = NULL WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                failedAttempts.remove(username);
                LOG.infof("🔓 Account unlocked for user: %s", username);
                return true;
            }
            return false;

        } catch (SQLException e) {
            LOG.error("❌ Error unlocking account: " + username, e);
            return false;
        }
    }

    // ==================== CHANGE PASSWORD ====================
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!authenticateUser(username, oldPassword)) {
            LOG.warnf("❌ Password change failed - invalid old password for user: %s", username);
            return false;
        }

        if (!isPasswordStrong(newPassword)) {
            LOG.warnf("❌ Password change failed - weak new password for user: %s", username);
            return false;
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
                    return true;
                }
                return false;
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("❌ Error hashing new password", e);
            return false;
        } catch (SQLException e) {
            LOG.error("❌ Database error", e);
            return false;
        }
    }

    // ==================== USER EXISTS ====================
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

    // ==================== GET USER COUNT ====================
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            LOG.error("❌ Error getting user count", e);
            return 0;
        }
    }

    // ==================== GET USER CREATED AT ====================
    public Instant getUserCreatedAt(String username) {
        String sql = "SELECT created_at FROM users WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Instant.parse(rs.getString("created_at"));
            }

        } catch (SQLException e) {
            LOG.error("❌ Error getting user created at", e);
        }

        return null;
    }

    // ==================== DELETE USER ====================
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                failedAttempts.remove(username);
                LOG.infof("🗑️ User deleted: %s", username);
                return true;
            }
            return false;

        } catch (SQLException e) {
            LOG.error("❌ Error deleting user", e);
            return false;
        }
    }

    // ==================== CLEAR DATABASE ====================
    public void clearDatabase() {
        String sql = "DELETE FROM users";

        try (Connection conn = SQLiteHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            failedAttempts.clear();
            LOG.info("🗑️ Database cleared");

        } catch (SQLException e) {
            LOG.error("❌ Error clearing database", e);
        }
    }

    // ==================== HELPER METHODS ====================
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

    private String hashPassword(String password, String salt)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String saltedPassword = salt + password;
        byte[] hashedBytes = md.digest(saltedPassword.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
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
}