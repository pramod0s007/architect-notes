/**
 * Result returned by the security pipeline.
 */
public final class ApiResponse {

    private final int statusCode;
    private final String message;
    private final String body;

    public ApiResponse(int statusCode, String message, String body) {
        this.statusCode = statusCode;
        this.message    = message;
        this.body       = body;
    }

    // ── Factory helpers ────────────────────────────────────────────────────────

    public static ApiResponse ok(String body) {
        return new ApiResponse(200, "OK", body);
    }

    public static ApiResponse forbidden(String reason) {
        return new ApiResponse(403, "Forbidden", reason);
    }

    public static ApiResponse unauthorized(String reason) {
        return new ApiResponse(401, "Unauthorized", reason);
    }

    public static ApiResponse tooManyRequests() {
        return new ApiResponse(429, "Too Many Requests", "Rate limit exceeded");
    }

    public static ApiResponse badRequest(String reason) {
        return new ApiResponse(400, "Bad Request", reason);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public String getBody()    { return body; }

    @Override
    public String toString() {
        return statusCode + " " + message + " — " + body;
    }
}
