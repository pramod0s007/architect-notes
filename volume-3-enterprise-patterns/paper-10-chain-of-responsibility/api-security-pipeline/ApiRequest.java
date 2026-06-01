import java.util.HashMap;
import java.util.Map;

/**
 * Immutable description of an inbound API call.
 * The {@code context} map is mutable so handlers can attach enrichment
 * (e.g. resolved userId, parsed claims) for downstream stages.
 */
public final class ApiRequest {

    private final String ip;
    private final String token;
    private final String userId;
    private final String endpoint;
    private final String method;
    private final String body;
    private final Map<String, Object> context;

    public ApiRequest(String ip, String token, String userId,
                      String endpoint, String method, String body) {
        this.ip       = ip;
        this.token    = token;
        this.userId   = userId;
        this.endpoint = endpoint;
        this.method   = method;
        this.body     = body;
        this.context  = new HashMap<>();
    }

    public String getIp()       { return ip; }
    public String getToken()    { return token; }
    public String getUserId()   { return userId; }
    public String getEndpoint() { return endpoint; }
    public String getMethod()   { return method; }
    public String getBody()     { return body; }

    /** Shared context bag — handlers can read and write enrichment data here. */
    public Map<String, Object> getContext() { return context; }

    @Override
    public String toString() {
        return method + " " + endpoint + " from " + ip +
               (userId.isEmpty() ? "" : " user=" + userId);
    }
}
