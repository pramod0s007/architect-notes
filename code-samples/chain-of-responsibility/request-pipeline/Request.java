/**
 * Request entering the pipeline.
 * See: volume-3/.../paper-10-chain-of-responsibility
 */
public final class Request {

    private final String method;
    private final String path;
    private final String userId;
    private final String token;
    private final String body;

    public Request(String method, String path, String userId, String token, String body) {
        this.method = method;
        this.path = path;
        this.userId = userId;
        this.token = token;
        this.body = body;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String userId() {
        return userId;
    }

    public String token() {
        return token;
    }

    public String body() {
        return body;
    }
}
