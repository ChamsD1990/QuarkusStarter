package AttendanceSystem.Service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.token.duration.hours", defaultValue = "8")
    int tokenDurationHours;

    @ConfigProperty(name = "jwt.secret.key", defaultValue = "mySecretKeyForJWTTokenGeneration")
    String secretKey;

    public String generateToken(String username) {
        try {
            // Header
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());

            // Payload
            Instant now = Instant.now();
            Instant expiry = now.plus(tokenDurationHours, ChronoUnit.HOURS);

            String payload = String.format(
                    "{\"sub\":\"%s\",\"iss\":\"attendance-system\",\"iat\":%d,\"exp\":%d,\"roles\":[\"user\"],\"username\":\"%s\"}",
                    username, now.getEpochSecond(), expiry.getEpochSecond(), username);
            String payloadEncoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());

            // Signature
            String data = header + "." + payloadEncoded;
            String signature = sign(data);

            return data + "." + signature;

        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String hmacData = data + secretKey;
        byte[] hash = md.digest(hmacData.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3)
                return false;

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            return expectedSignature.equals(parts[2]);

        } catch (Exception e) {
            return false;
        }
    }
}