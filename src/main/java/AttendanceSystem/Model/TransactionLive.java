package AttendanceSystem.Model;

public class TransactionLive {
    private String id;
    private String cardNo;
    private String trName; 

    public TransactionLive() {
    }

    public TransactionLive(String id, String cardNo, String trName) {
        this.id = id;
        this.cardNo = cardNo;
        this.trName = trName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getTrName() {
        return trName;
    }

    public void setTrName(String TrName) {
        this.trName = TrName;
    }
 

}