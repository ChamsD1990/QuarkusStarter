package AttendanceSystem.Service.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLServerHelper {

    private static final String DB_URL = "jdbc:sqlserver://NURCHAMDANI;databaseName=DataDBENT;encrypt=true;trustServerCertificate=true";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "Admin123!@#";

    private static Connection connection = null;

    // ==================== GET CONNECTION ====================
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                connection.setAutoCommit(true);
                System.out.println("✅ SQL Server Connected!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQL Server JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
            connection = null;
        }
        return connection;
    }

    // ==================== SELECT / QUERY ====================
    public static ResultSet executeQuery(String query) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("❌ Query failed: " + query);
            e.printStackTrace();
            return null;
        }
    }

    // ==================== SELECT WITH PARAMETERS ====================
    public static ResultSet executeQuery(String query, Object... params) {
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("❌ Query failed: " + query);
            e.printStackTrace();
            return null;
        }
    }

    // ==================== INSERT, UPDATE, DELETE ====================
    public static int executeUpdate(String query) {
        try {
            Statement stmt = getConnection().createStatement();
            return stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("❌ Update failed: " + query);
            e.printStackTrace();
            return -1;
        }
    }

    // ==================== INSERT, UPDATE, DELETE WITH PARAMETERS
    // ====================
    public static int executeUpdate(String query, Object... params) {
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Update failed: " + query);
            e.printStackTrace();
            return -1;
        }
    }

    // ==================== INSERT WITH RETURN ID ====================
    public static int executeInsert(String query, Object... params) {
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("❌ Insert failed: " + query);
            e.printStackTrace();
            return -1;
        }
    }

    // ==================== CLOSE CONNECTION ====================
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                connection = null;
                System.out.println("✅ SQL Server connection closed.");
            } catch (SQLException e) {
                System.err.println("❌ Error closing connection!");
                e.printStackTrace();
            }
        }
    }

    // ==================== CHECK CONNECTION ====================
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ==================== TEST ====================
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("✅ SQL Server connection successful!");
                System.out.println("📁 Database: DataDBENT");
                System.out.println("🔌 Status: " + isConnected());
            } else {
                System.out.println("❌ SQL Server connection failed!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}