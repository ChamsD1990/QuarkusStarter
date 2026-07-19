package AttendanceSystem.Repository;

import AttendanceSystem.Model.TransactionLive;
import AttendanceSystem.common.DBSetContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TransactionLiveRepository {

    @Inject
    DBSetContext dbContext;

    public List<TransactionLive> getAllTransactions() {
        List<TransactionLive> transactions = new ArrayList<>();
        String sql = "SELECT TOP(10) * FROM dbo.tblTransactionLive where CardNo NOT IN ('FFFFFFFFFF')"; 
        try (Connection conn = dbContext.getSqlServerConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) { 
            while (rs.next()) {
                TransactionLive tx = new TransactionLive();
                tx.setId(rs.getString("ID"));
                tx.setCardNo(rs.getString("CardNo"));
                tx.setTrName(rs.getString("TrName"));
                transactions.add(tx);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch transactions", e);
        } 
        return transactions;
    }
 
    public TransactionLive getTransactionById(String id) {
      String sql = "SELECT * FROM dbo.tblTransactionLive WHERE CardNo = ?"; 
      try (Connection conn = dbContext.getSqlServerConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) { 
            stmt.setString(1, id); 
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TransactionLive tx = new TransactionLive();
                    tx.setId(rs.getString("ID"));
                    tx.setCardNo(rs.getString("CardNo"));
                    tx.setTrName(rs.getString("TrName"));
                    return tx;
                }
            } 
        } catch (SQLException e) {
            System.err.println("❌ Error fetching transaction by ID: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch transaction with ID: " + id, e);
        }

        return null;
    }

    // Optional: Add method to get transactions by CardNo
    public List<TransactionLive> getTransactionsByCardNo(String cardNo) {
        List<TransactionLive> transactions = new ArrayList<>();
        String sql = "SELECT * FROM dbo.tblTransactionLive WHERE CardNo = ?";

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cardNo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TransactionLive tx = new TransactionLive();
                    tx.setId(rs.getString("ID"));
                    tx.setCardNo(rs.getString("CardNo"));
                    tx.setTrName(rs.getString("TrName"));
                    transactions.add(tx);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching transactions by CardNo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch transactions for CardNo: " + cardNo, e);
        }

        return transactions;
    }
}