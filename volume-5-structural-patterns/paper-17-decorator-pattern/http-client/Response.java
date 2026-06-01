import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP response.
 */
public class Response {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public Response(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body       = body;
        this.headers    = headers != null
                ? Collections.unmodifiableMap(new HashMap<>(headers))
                : Collections.emptyMap();
    }

    public Response(int statusCode, String body) {
        this(statusCode, body, Collections.emptyMap());
    }

    public int               getStatusCode() { return statusCode; }
    public String            getBody()       { return body; }
    public Map<String,String> getHeaders()   { return headers; }

    public boolean isSuccess()    { return statusCode >= 200 && statusCode < 300; }
    public boolean isServerError(){ return statusCode >= 500 && statusCode < 600; }

    @Override
    public String toString() {
        return "Response{statusCode=" + statusCode
               + ", body='" + body + '\''
               + ", headers=" + headers + '}';
    }
}
