/**
 * Decorator that logs method, URL, HTTP status, and elapsed time for every
 * request. Wraps any {@link HttpClient} implementation without modifying it.
 */
public class LoggingDecorator implements HttpClient {

    private final HttpClient delegate;

    public LoggingDecorator(HttpClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response get(String url) {
        long start = System.currentTimeMillis();
        Response response = delegate.get(url);
        long elapsed = System.currentTimeMillis() - start;
        log("GET", url, response, elapsed);
        return response;
    }

    @Override
    public Response post(String url, String body) {
        long start = System.currentTimeMillis();
        Response response = delegate.post(url, body);
        long elapsed = System.currentTimeMillis() - start;
        log("POST", url, response, elapsed);
        return response;
    }

    private void log(String method, String url, Response response, long elapsedMs) {
        String level = response.isSuccess() ? "INFO" : "WARN";
        System.out.printf("  [Logging][%s] %s %s -> %d (%dms)%n",
                level, method, url, response.getStatusCode(), elapsedMs);
    }
}
