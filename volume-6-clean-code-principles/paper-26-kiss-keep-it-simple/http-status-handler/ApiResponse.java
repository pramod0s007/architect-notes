/**
 * KISS: simple API response envelope — 3 fields, 4 factory methods.
 * Handles every real use case cleanly. Compare to OverEngineeredApiResponse.
 */
public class ApiResponse {

    private final int statusCode;
    private final String message;
    private final Object data;

    private ApiResponse(int statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse ok(Object data) {
        return new ApiResponse(200, "OK", data);
    }

    public static ApiResponse notFound(String message) {
        return new ApiResponse(404, message, null);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(500, message, null);
    }

    public static ApiResponse badRequest(String message) {
        return new ApiResponse(400, message, null);
    }

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Object getData()    { return data; }

    @Override
    public String toString() {
        return "ApiResponse{" + statusCode + " " + message
                + (data != null ? ", data=" + data : "") + "}";
    }
}
