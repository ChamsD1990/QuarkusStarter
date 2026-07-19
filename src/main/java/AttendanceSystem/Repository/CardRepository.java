package AttendanceSystem.Repository;

import AttendanceSystem.Model.CardDB;
import AttendanceSystem.common.DBSetContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CardRepository {

    @Inject
    DBSetContext dbContext;
 
    public List<CardDB> getAllCardDB() {
        List<CardDB> cards = new ArrayList<>();
        String sql = "SELECT TOP(10) ID, CardNo, Name, SiteCode, Department, Position, Email, MobileNo, Status, CreatedDate FROM dbo.CardDB";

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CardDB card = mapResultSet(rs);
                cards.add(card);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching cards: " + e.getMessage());
            e.printStackTrace();
        }

        return cards;
    }
 
    public CardDB getCardById(String id) {
        String sql = "SELECT ID, CardNo, Name, SiteCode, Department, Position, Email, "
            +"MobileNo, Status, CreatedDate FROM dbo.CardDB WHERE CardNo = ?";

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting card: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
 
    public List<CardDB> getCardsByCardNo(String cardNo) {
        List<CardDB> cards = new ArrayList<>();
        String sql = "SELECT ID, CardNo, Name, SiteCode, Department, Position, Email, MobileNo, Status, CreatedDate FROM dbo.CardDB WHERE CardNo = ?";

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cardNo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error searching cards: " + e.getMessage());
            e.printStackTrace();
        }

        return cards;
    }
 
    public int insertCard(CardDB card) {
        String sql = "INSERT INTO dbo.CardDB (ID, CardNo, Name, SiteCode, Department, Position, Email, MobileNo, Status, CreatedDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        int rowsAffected = 0;

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, card.getID());
            stmt.setString(2, card.getCardNo());
            stmt.setString(3, card.getName());
            stmt.setString(4, card.getSiteCode());
            stmt.setString(5, card.getDepartment());
            stmt.setString(6, card.getPosition());
            stmt.setString(7, card.getEmail());
            stmt.setString(8, card.getMobileNo()); 

            rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Card inserted: " + card.getID());

        } catch (SQLException e) {
            System.err.println("❌ Error inserting card: " + e.getMessage());
            e.printStackTrace();
        }

        return rowsAffected;
    }
 
    public int updateCard(CardDB card) {
        String sql = "UPDATE dbo.CardDB SET CardNo = ?, Name = ?, SiteCode = ?, Department = ?, Position = ?, Email = ?, MobileNo = ?, Status = ?, LastUpdate = GETDATE() WHERE ID = ?";
        int rowsAffected = 0;

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, card.getCardNo());
            stmt.setString(2, card.getName());
            stmt.setString(3, card.getSiteCode());
            stmt.setString(4, card.getDepartment());
            stmt.setString(5, card.getPosition());
            stmt.setString(6, card.getEmail());
            stmt.setString(7, card.getMobileNo()); 
            stmt.setString(9, card.getID());

            rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Card updated: " + card.getID());

        } catch (SQLException e) {
            System.err.println("❌ Error updating card: " + e.getMessage());
            e.printStackTrace();
        }

        return rowsAffected;
    }
 
    public int deleteCard(String id) {
        String sql = "DELETE FROM dbo.CardDB WHERE ID = ?";
        int rowsAffected = 0;

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Card deleted: " + id);

        } catch (SQLException e) {
            System.err.println("❌ Error deleting card: " + e.getMessage());
            e.printStackTrace();
        }

        return rowsAffected;
    }
  
    public int uploadImage(String cardNo, byte[] imageData) {
        int rowsAffected = 0; 
        try (Connection conn = dbContext.getSqlServerConnection()) { 
            String checkSql = "SELECT COUNT(*) FROM dbo.CardDB WHERE CardNo = ?";
            boolean cardExists = false; 
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, cardNo);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        cardExists = rs.getInt(1) > 0;
                    }
                }
            } 
            if (!cardExists) {
                String insertSql = "INSERT INTO dbo.CardDB (CardNo, Photo, CreatedDate) VALUES (?, ?, GETDATE())";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, cardNo);
                    if (imageData != null && imageData.length > 0) {
                        insertStmt.setBytes(2, imageData);
                    } else {
                        insertStmt.setNull(2, java.sql.Types.VARBINARY);
                    }
                    rowsAffected = insertStmt.executeUpdate();
                    System.out.println("✅ New card inserted with photo: " + cardNo);
                }
            } else { 
                String updateSql = "UPDATE dbo.CardDB SET Photo = ?, LastUpdate = GETDATE() WHERE CardNo = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    if (imageData != null && imageData.length > 0) {
                        updateStmt.setBytes(1, imageData);
                    } else {
                        updateStmt.setNull(1, java.sql.Types.VARBINARY);
                    }
                    updateStmt.setString(2, cardNo);
                    rowsAffected = updateStmt.executeUpdate();
                    System.out.println("✅ Photo updated for existing card: " + cardNo);
                }
            } 
        } catch (SQLException e) {
            System.err.println("❌ Error uploading image: " + e.getMessage());
            e.printStackTrace();
        } 
        return rowsAffected;
    }
 
    public byte[] getImage(String cardId) {
        String sql = "SELECT Photo FROM dbo.CardDB WHERE ID = ?";

        try (Connection conn = dbContext.getSqlServerConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cardId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("Photo");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting image: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    } 

    private CardDB mapResultSet(ResultSet rs) throws SQLException {
        CardDB card = new CardDB();
        card.setID(rs.getString("ID"));
        card.setCardNo(rs.getString("CardNo"));
        card.setName(rs.getString("Name"));
        card.setSiteCode(rs.getString("SiteCode"));
        card.setDepartment(rs.getString("Department"));
        card.setPosition(rs.getString("Position"));
        card.setEmail(rs.getString("Email"));
        card.setMobileNo(rs.getString("MobileNo")); 
        card.setCreatedDate(rs.getTimestamp("CreatedDate"));
        return card;
    }
}