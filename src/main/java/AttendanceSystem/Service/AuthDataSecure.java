package AttendanceSystem.Service;

import io.quarkus.arc.Unremovable;
// HAPUS import ini
// import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
// HAPUS import ini
// import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Unremovable
public class AuthDataSecure {

    private static final Logger LOG = Logger.getLogger(AuthDataSecure.class);

    // HAPUS baris ini
    // @Inject
    // SecurityIdentity securityIdentity;

    @ConfigProperty(name = "auth.salt.length", defaultValue = "16")
    int saltLength;

    @ConfigProperty(name = "auth.max.failed.attempts", defaultValue = "5")
    int maxFailedAttempts;

    @ConfigProperty(name = "auth.lockout.duration.minutes", defaultValue = "15")
    int lockoutDurationMinutes;

    private final Map<String, UserCredential> userDatabase = new ConcurrentHashMap<>();
    private final Map<String, FailedLoginAttempt> failedAttempts = new ConcurrentHashMap<>();

    private static final SecureRandom secureRandom = new SecureRandom();

    private static class UserCredential {
        private final String salt;
        private final String hashedPassword;
        private final Instant createdAt;
        private boolean isLocked;
        private Instant lockedUntil;

        public UserCredential(String salt, String hashedPassword) {
            this.salt = salt;
            this.hashedPassword = hashedPassword;
            this.createdAt = Instant.now();
            this.isLocked = false;
            this.lockedUntil = null;
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

        public void setLocked(boolean locked) {
            isLocked = locked;
        }

        public Instant getLockedUntil() {
            return lockedUntil;
        }

        public void setLockedUntil(Instant lockedUntil) {
            this.lockedUntil = lockedUntil;
        }
    }

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

    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            LOG.warn("Registration attempt with empty username");
            return false;
        }

        if (!isPasswordStrong(password)) {
            LOG.warnf("Weak password attempt for user: %s", username);
            return false;
        }

        if (userDatabase.containsKey(username)) {
            LOG.warnf("Registration attempt for existing user: %s", username);
            return false;
        }

        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);
            userDatabase.put(username, new UserCredential(salt, hashedPassword));
            LOG.infof("User registered successfully: %s", username);
            return true;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error hashing password", e);
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        UserCredential user = userDatabase.get(username);
        if (user == null) {
            recordFailedAttempt(username);
            return false;
        }

        if (user.isLocked()) {
            if (user.getLockedUntil() != null && Instant.now().isBefore(user.getLockedUntil())) {
                LOG.warnf("Account locked for user: %s until %s", username, user.getLockedUntil());
                return false;
            } else {
                user.setLocked(false);
                user.setLockedUntil(null);
                failedAttempts.remove(username);
            }
        }

        try {
            String hashedInput = hashPassword(password, user.getSalt());
            boolean authenticated = hashedInput.equals(user.getHashedPassword());

            if (authenticated) {
                failedAttempts.remove(username);
                LOG.infof("User authenticated successfully: %s", username);
                return true;
            } else {
                recordFailedAttempt(username);
                LOG.warnf("Failed login attempt for user: %s", username);
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error hashing password", e);
            return false;
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
            UserCredential user = userDatabase.get(username);
            if (user != null) {
                user.setLocked(true);
                user.setLockedUntil(Instant.now().plusSeconds(lockoutDurationMinutes * 60));
                LOG.warnf("Account locked for user: %s after %d failed attempts",
                        username, maxFailedAttempts);
            }
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

    private String hashPassword(String password, String salt)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] hashedBytes = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!authenticateUser(username, oldPassword)) {
            LOG.warnf("Password change failed - invalid old password for user: %s", username);
            return false;
        }

        if (!isPasswordStrong(newPassword)) {
            LOG.warnf("Password change failed - weak new password for user: %s", username);
            return false;
        }

        try {
            String newSalt = generateSalt();
            String newHashedPassword = hashPassword(newPassword, newSalt);

            UserCredential user = userDatabase.get(username);
            if (user != null) {
                UserCredential newCred = new UserCredential(newSalt, newHashedPassword);
                userDatabase.put(username, newCred);
                LOG.infof("Password changed successfully for user: %s", username);
                return true;
            }
            return false;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error hashing new password", e);
            return false;
        }
    }

    public Instant getUserCreatedAt(String username) {
        UserCredential user = userDatabase.get(username);
        return user != null ? user.getCreatedAt() : null;
    }

    public boolean unlockAccount(String username) {
        UserCredential user = userDatabase.get(username);
        if (user != null && user.isLocked()) {
            user.setLocked(false);
            user.setLockedUntil(null);
            failedAttempts.remove(username);
            LOG.infof("Account unlocked for user: %s by admin", username);
            return true;
        }
        return false;
    }

    public boolean deleteUser(String username) {
        UserCredential removed = userDatabase.remove(username);
        failedAttempts.remove(username);
        if (removed != null) {
            LOG.infof("User deleted: %s", username);
            return true;
        }
        return false;
    }

    public void clearDatabase() {
        userDatabase.clear();
        failedAttempts.clear();
        LOG.info("Database cleared");
    }

    public boolean userExists(String username) {
        return userDatabase.containsKey(username);
    }

    public int getUserCount() {
        return userDatabase.size();
    }
}