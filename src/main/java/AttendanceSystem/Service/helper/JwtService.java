package AttendanceSystem.Service.helper;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.token.duration.hours", defaultValue = "8")
    int tokenDurationHours;

    @ConfigProperty(name = "jwt.secret.key", defaultValue = "mySecretKeyForJWTTokenGeneration")
    String secretKey;

    private static final String ALGORITHM = "HmacSHA256";

    // ============================================================
    // GENERATE TOKEN - MAIN METHOD (Semua mengarah ke sini)
    // ============================================================
    public String generateToken(String userId, String role, String ip) {
        try {
            // Header
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

            // Payload dengan lebih banyak informasi
            Instant now = Instant.now();
            Instant expiry = now.plus(tokenDurationHours, ChronoUnit.HOURS);
            String jti = UUID.randomUUID().toString();

            String payload = String.format(
                    "{\"sub\":\"%s\",\"iss\":\"attendance-system\",\"iat\":%d,\"exp\":%d,\"jti\":\"%s\",\"role\":\"%s\",\"ip\":\"%s\"}",
                    userId,
                    now.getEpochSecond(),
                    expiry.getEpochSecond(),
                    jti,
                    role,
                    ip);

            String payloadEncoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

            String data = header + "." + payloadEncoded;
            String signature = sign(data);

            return data + "." + signature;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // GENERATE TOKEN - OVERLOAD METHODS
    // ============================================================

    // 1 parameter: username only
    public String generateToken(String username) {
        return generateToken(username, "user", "0.0.0.0");
    }

    // 2 parameters: username + role
    public String generateToken(String username, String role) {
        return generateToken(username, role, "0.0.0.0");
    }

    // 3 parameters: username + role + ip (ini yang utama)
    // Sudah ada di atas

    // ============================================================
    // SIGN WITH HMAC SHA256 (SECURE)
    // ============================================================
    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                ALGORITHM);
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

    // ============================================================
    // VALIDATE TOKEN - FULL VERSION
    // ============================================================
    public boolean validateToken(String token, String clientIp) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            // 1. Validate signature
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                return false;
            }

            // 2. Decode payload
            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);

            // 3. Check expiration
            long exp = extractLongValue(payloadJson, "exp");
            if (exp < Instant.now().getEpochSecond()) {
                return false; // Token expired
            }

            // 4. Check IP binding (if clientIp provided)
            if (clientIp != null && !clientIp.equals("0.0.0.0")) {
                String tokenIp = extractStringValue(payloadJson, "ip");
                if (tokenIp != null && !tokenIp.isEmpty() && !tokenIp.equals("0.0.0.0")) {
                    if (!tokenIp.equals(clientIp)) {
                        return false; // IP mismatch
                    }
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // VALIDATE TOKEN - SIMPLE VERSION (Backward Compatible)
    // ============================================================
    public boolean validateToken(String token) {
        return validateToken(token, null);
    }

    // ============================================================
    // EXTRACT VALUE FROM PAYLOAD
    // ============================================================
    private long extractLongValue(String payloadJson, String key) {
        try {
            String pattern = "\"" + key + "\":([0-9]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(payloadJson);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String extractStringValue(String payloadJson, String key) {
        try {
            String pattern = "\"" + key + "\":\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(payloadJson);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ============================================================
    // GET USER ID FROM TOKEN
    // ============================================================
    public String getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);

            return extractStringValue(payloadJson, "sub");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // GET ROLE FROM TOKEN
    // ============================================================
    public String getRoleFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);

            return extractStringValue(payloadJson, "role");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // GET IP FROM TOKEN
    // ============================================================
    public String getIpFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);

            return extractStringValue(payloadJson, "ip");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // GET EXPIRATION FROM TOKEN
    // ============================================================
    public long getExpirationFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return 0;
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);

            return extractLongValue(payloadJson, "exp");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============================================================
    // CHECK IF TOKEN IS EXPIRED
    // ============================================================
    public boolean isTokenExpired(String token) {
        long exp = getExpirationFromToken(token);
        return exp < Instant.now().getEpochSecond();
    }

    // ============================================================
    // REFRESH TOKEN (Generate new token with same data)
    // ============================================================
    public String refreshToken(String oldToken) {
        String userId = getUserIdFromToken(oldToken);
        String role = getRoleFromToken(oldToken);
        String ip = getIpFromToken(oldToken);

        if (userId == null) {
            return null;
        }

        return generateToken(userId, role != null ? role : "user", ip != null ? ip : "0.0.0.0");
    }

    // ============================================================
    // GENERATE SCRAPE TOKEN (for anti-scraping)
    // ============================================================
    public String generateScrapeToken(String ip) {
        String random = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String data = ip + ":" + random + ":" + timestamp;

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String signatureBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signature);

            return data + ":" + signatureBase64;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // VALIDATE SCRAPE TOKEN
    // ============================================================
    public boolean validateScrapeToken(String token, String clientIp) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            String[] parts = token.split(":");
            if (parts.length != 4) {
                return false;
            }

            String ip = parts[0];
            String random = parts[1];
            String timestamp = parts[2];
            String signature = parts[3];

            // Check IP
            if (!ip.equals(clientIp)) {
                return false;
            }

            // Check timestamp (max 5 minutes)
            long tokenTime = Long.parseLong(timestamp);
            long currentTime = Instant.now().getEpochSecond();
            if (currentTime - tokenTime > 300) {
                return false; // Token expired (5 minutes)
            }

            // Verify signature
            String data = ip + ":" + random + ":" + timestamp;
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    ALGORITHM);
            mac.init(secretKeySpec);
            byte[] expectedSignature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expectedSignatureBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(expectedSignature);

            return signature.equals(expectedSignatureBase64);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // GENERATE CSRF TOKEN
    // ============================================================
    public String generateCsrfToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                ":" +
                Instant.now().getEpochSecond();
    }

    // ============================================================
    // VALIDATE CSRF TOKEN
    // ============================================================
    public boolean validateCsrfToken(String token, String storedToken) {
        if (token == null || storedToken == null) {
            return false;
        }
        return token.equals(storedToken);
    }
}