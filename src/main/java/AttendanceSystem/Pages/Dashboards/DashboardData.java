package AttendanceSystem.Pages.Dashboards;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardData {
    public String title;
    public String username;
    public String lastLogin;
    public int totalEmployees;
    public int presentToday;
    public int absentToday;
    public int attendanceRate;
    public String currentYear;
    public String version;
    public String serverTime;
    public List<Activity> recentActivities;

    // Constructor
    public DashboardData() {
        this.currentYear = String.valueOf(LocalDateTime.now().getYear());
        this.version = "1.0.0";
        this.serverTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Inner class for activities
    public static class Activity {
        public String type;
        public String action;
        public String user;
        public String time;

        public Activity(String type, String user, String time) {
            this.type = type;
            this.action = type.substring(0, 1).toUpperCase() + type.substring(1);
            this.user = user;
            this.time = time;
        }
    }
}