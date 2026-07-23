package AttendanceSystem.Service.helper;

import jakarta.ws.rs.core.HttpHeaders;

public interface DedicatedIPService {

    String getClientIp(HttpHeaders headers);

    boolean isBlocked(String ip);

    boolean isWhitelisted(String ip);

    boolean isRateLimited(String ip);

    void blockIp(String ip);

    void unblockIp(String ip);

    void whitelistIp(String ip);

    int getRequestCount(String ip);

    boolean isValidIp(String ip);

    boolean isLocalIp(String ip);
}