package AttendanceSystem.Service.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class SQLiteHelper {

    private static final String DB_URL = "jdbc:sqlite:./attendance.db";
    private static Connection connection = null;
    private static boolean tablesCreated = false;

    // GET CONNECTION - AUTO RECONNECT
    public static synchronized Connection getConnection() {
        try {
            // CEK: Kalo connection null ATAU closed, buat baru
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true);

                // Bikin tabel cuma sekali
                if (!tablesCreated) {
                    createTables();
                    tablesCreated = true;
                }

                System.out.println("✅ SQLite Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite JDBC Driver not found!");
            e.printStackTrace();
            connection = null;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
            connection = null;
        }
        return connection;
    }

    private static void createTables() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    salt TEXT NOT NULL,
                    hashed_password TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    is_locked INTEGER DEFAULT 0,
                    locked_until TEXT
                );
                """;

        String sessionTable = """
                CREATE TABLE IF NOT EXISTS sessionData (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    session_created TEXT NOT NULL,
                    session_cleared TEXT NOT NULL,
                    session_expired TEXT NOT NULL
                );
                """;

        String createAttendanceTable = """
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    employee_id TEXT NOT NULL,
                    employee_name TEXT NOT NULL,
                    check_in_time TEXT NOT NULL,
                    check_out_time TEXT,
                    date TEXT NOT NULL,
                    status TEXT DEFAULT 'present'
                );
                """;

        String createEmployeeTable = """
                CREATE TABLE IF NOT EXISTS employees (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    department TEXT,
                    position TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createAttendanceTable);
            stmt.execute(createEmployeeTable);
            stmt.execute(sessionTable);
            System.out.println("✅ Tables created/verified successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error creating tables!");
            e.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String query) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("❌ Query execution failed: " + query);
            e.printStackTrace();
            return null;
        }
    }

    public static int executeUpdate(String query) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("❌ Update execution failed: " + query);
            e.printStackTrace();
            return -1;
        }
    }

    // JANGAN PANGGIL INI KECUALI APP SHUTDOWN!
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                connection = null;
                tablesCreated = false;
                System.out.println("✅ Database connection closed.");
            } catch (SQLException e) {
                System.err.println("❌ Error closing connection!");
                e.printStackTrace();
            }
        }
    }

    // CEK KONEKSI MASIH IDUP
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // public static void main(String[] args) {
    //     Connection conn = getConnection();
    //     if (conn != null) {
    //         System.out.println("✅ Database connection successful!");
    //         System.out.println("📁 Database file: attendance.db");
    //         System.out.println("🔌 Connection status: " + isConnected());
    //     } else {
    //         System.out.println("❌ Database connection failed!");
    //     }
    //     // JANGAN CLOSE DI MAIN!
    //     // closeConnection();
    // }
}