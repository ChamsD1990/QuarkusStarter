package AttendanceSystem.Model;
 
public class SessionData {
    public int requestCount;
    public long lastRequest;

    public SessionData(int count, long time) {
        this.requestCount = count;
        this.lastRequest = time;
    }
    
    // Getter & Setter (Opsional)
    public int getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
    
    public long getLastRequest() {
        return lastRequest;
    }
    
    public void setLastRequest(long lastRequest) {
        this.lastRequest = lastRequest;
    }
}