package AttendanceSystem.Model;

public class ResultResponse<T> {
    private String message;
    private boolean success;
    private T data;
    private String timestamp;

    // ✅ Default constructor
    public ResultResponse() {
        this.success = true;
        this.message = "Success";
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // ✅ Constructor dengan data (untuk SELECT)
    public ResultResponse(T data) {
        this();
        this.data = data;
    }

    // ✅ Constructor untuk error
    public ResultResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // ✅ Constructor dengan data dan message
    public ResultResponse(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // ✅ Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // ✅ Helper methods untuk memudahkan
    public static <T> ResultResponse<T> success(T data) {
        return new ResultResponse<>(data);
    }

    public static <T> ResultResponse<T> success(T data, String message) {
        return new ResultResponse<>(data, message, true);
    }

    public static <T> ResultResponse<T> error(String message) {
        return new ResultResponse<>(message, false);
    }

    public static <T> ResultResponse<T> error(String message, Exception e) {
        return new ResultResponse<>(message + " - " + e.getMessage(), false);
    }

    @Override
    public String toString() {
        return "ResultResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", data=" + data +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}