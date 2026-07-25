package AttendanceSystem.Service.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class SQLiteHelper {

    public SQLiteHelper(){}
    private static final String DB_URL = "jdbc:sqlite:./attendance.db";
    private static Connection connection = null;
    
    public static Connection getConnection() {
        if (connection == null) {
            try { 
                Class.forName("org.sqlite.JDBC"); 
                connection = DriverManager.getConnection(DB_URL); 
                createTables();

                System.out.println("SQLite Database connected successfully!");

            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC Driver not found!");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Database connection failed!");
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Create required tables
    private static void createTables() {
        String sessionTable = """
            CREATE TABLE IF NOT EXISTS sessionData (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                session_created TEXT NOT NULL,
                session_cleared TEXT NOT NULL
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
            stmt.execute(createAttendanceTable);
            stmt.execute(createEmployeeTable);
            stmt.execute(sessionTable);
            System.out.println("Tables created/verified successfully!");
        } catch (SQLException e) {
            System.err.println("Error creating tables!");
            e.printStackTrace();
        }
    }

    // Execute query (SELECT)
    public static ResultSet executeQuery(String query) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("Query execution failed: " + query);
            e.printStackTrace();
            return null;
        }
    }

    // Execute update (INSERT, UPDATE, DELETE)
    public static int executeUpdate(String query) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Update execution failed: " + query);
            e.printStackTrace();
            return -1;
        }
    }

    // Close connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection!");
                e.printStackTrace();
            }
        }
    }

    // Test method
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("✅ Database connection successful!");
            System.out.println("Database file: attendance.db");
        } else {
            System.out.println("❌ Database connection failed!");
        }
        closeConnection();
    }
}