package AttendanceSystem.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import AttendanceSystem.Service.helper.SQLiteHelper;

public class SessionService {

    private static final long ONE_MINUTE_IN_SECONDS = 60;

    // Method utama untuk cek session login
    public static boolean isSessionRunning(Instant sessionCreated,
            Instant sessionMax,
            Instant now) {
        long durationInSeconds = ChronoUnit.SECONDS.between(sessionCreated, now);
        boolean isMoreThanOneMinute = durationInSeconds >= 60;
        boolean isWithinMaxLimit = (sessionMax == null || now.isBefore(sessionMax));

        return isMoreThanOneMinute && isWithinMaxLimit;
    }

    // Method untuk mengecek session dari database
    public static Boolean runSession() {
        boolean isRunning = false;
        Connection conn = null;

        try {
            conn = SQLiteHelper.getConnection();
            String checkQuery = "SELECT session_created, session_max FROM sessionData " +
                    "WHERE session_created IS NOT NULL " +
                    "AND isAuthenticated = 1 " + // Hanya session aktif
                    "AND isLogout = 1 " + // Hanya session aktif
                    "ORDER BY id DESC " + // Ambil session terbaru
                    "LIMIT 1";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(checkQuery)) {

                if (rs.next()) {
                    String sessionCreatedStr = rs.getString("session_created");
                    String sessionMaxStr = rs.getString("session_max");

                    Instant sessionCreated = Instant.parse(sessionCreatedStr);
                    Instant sessionMax = sessionMaxStr != null ? Instant.parse(sessionMaxStr) : null;

                    Instant now = Instant.now();
                    isRunning = isSessionRunning(sessionCreated, sessionMax, now);

                    // Log untuk debugging
                    System.out.println("=== Session Check ===");
                    System.out.println("Session Running: " + isRunning);

                } else {
                    System.out.println("No active session found");
                    isRunning = false;
                }
            }

        } catch (Exception e) {
            System.err.println("Error in runSession: " + e.getMessage());
            isRunning = false;
        }

        return isRunning;
    }

    // Method untuk validasi login dengan session check
    public static boolean isValidSession() {
        boolean isValid = runSession();
        System.out.println("Session validation result: " + isValid);
        return isValid;
    }
}