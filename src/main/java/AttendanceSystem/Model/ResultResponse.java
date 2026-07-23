package AttendanceSystem.Model;

public class ResultResponse {
    private String status;
    private Object data;
    private String message;

    public ResultResponse() {
    }

    public ResultResponse(String status, Object data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static ResultResponse success(Object data) {
        return new ResultResponse("success", data, null);
    }

    public static ResultResponse success(String message) {
        return new ResultResponse("success", null, message);
    }

    public static ResultResponse error(String message) {
        return new ResultResponse("error", null, message);
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}