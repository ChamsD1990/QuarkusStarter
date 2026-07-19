package AttendanceSystem.common;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@ApplicationScoped
public class DBSetContext {

    private static final String SQL_SERVER_URL = "jdbc:sqlserver://NURCHAMDANI;"
            + "databaseName=DataDBENT;"
            + "encrypt=false;"
            + "trustServerCertificate=true;"
            + "loginTimeout=30;"; 
    private static final String SQL_SERVER_USERNAME = "sa";
    private static final String SQL_SERVER_PASSWORD = "Admin123!@#";  
    private static final String SQLITE_URL = "jdbc:sqlite:attendance.db";

    public Connection getSqlServerConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("========================================");
            System.out.println("🔗 Connecting to SQL Server...");
            System.out.println("   Server: NURCHAMDANI");
            System.out.println("   Database: DataDBENT");
            System.out.println("   Username: " + SQL_SERVER_USERNAME);
            System.out.println("   Password: " + "*".repeat(SQL_SERVER_PASSWORD.length()));
            System.out.println("========================================");

            Connection conn = DriverManager.getConnection(
                    SQL_SERVER_URL,
                    SQL_SERVER_USERNAME,
                    SQL_SERVER_PASSWORD);

            System.out.println("✅ Database connection successful!");
            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("❌ SQL Server connection failed!");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Error Code: " + e.getErrorCode());

            // Provide helpful suggestions
            if (e.getErrorCode() == 18456) {
                System.err.println("🔍 This error means:");
                System.err.println("   - Wrong username or password");
                System.err.println("   - 'sa' login is disabled");
                System.err.println("   - SQL Server is in Windows Authentication mode only");
                System.err.println("   - The database doesn't exist");
                System.err.println("   ");
                System.err.println("💡 Try these steps:");
                System.err.println("   1. Connect to SQL Server with Windows Authentication");
                System.err.println("   2. Run: ALTER LOGIN sa ENABLE;");
                System.err.println("   3. Run: ALTER LOGIN sa WITH PASSWORD = 'admin1234';");
                System.err.println("   4. Restart SQL Server service");
            }
            throw e;
        }
    }

    public Connection getSQLiteConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(SQLITE_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found", e);
        }
    }
}