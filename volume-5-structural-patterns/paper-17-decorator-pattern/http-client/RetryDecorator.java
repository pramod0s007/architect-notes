/**
 * Decorator that transparently retries requests that receive a 5xx response.
 *
 * Uses exponential back-off between attempts. GET and POST are both retried
 * because the underlying simulated server is idempotent in this example;
 * in production you would typically retry only idempotent methods (GET, PUT).
 */
public class RetryDecorator implements HttpClient {

    private final HttpClient delegate;
    private final int maxRetries;
    private final long initialBackoffMs;

    public RetryDecorator(HttpClient delegate, int maxRetries, long initialBackoffMs) {
        this.delegate         = delegate;
        this.maxRetries       = maxRetries;
        this.initialBackoffMs = initialBackoffMs;
    }

    @Override
    public Response get(String url) {
        return executeWithRetry("GET", url, null);
    }

    @Override
    public Response post(String url, String body) {
        return executeWithRetry("POST", url, body);
    }

    private Response executeWithRetry(String method, String url, String body) {
        long backoff = initialBackoffMs;
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            Response response = "POST".equals(method)
                    ? delegate.post(url, body)
                    : delegate.get(url);

            if (!response.isServerError()) {
                if (attempt > 1) {
                    System.out.println("  [Retry] Succeeded on attempt " + attempt);
                }
                return response;
            }

            if (attempt <= maxRetries) {
                System.out.println("  [Retry] " + method + " " + url
                        + " returned " + response.getStatusCode()
                        + " — retrying in " + backoff + "ms (attempt " + attempt + "/" + (maxRetries + 1) + ")");
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return response;
                }
                backoff *= 2;
            } else {
                System.out.println("  [Retry] Exhausted " + maxRetries + " retries for "
                        + method + " " + url + " — giving up");
            }
        }
        // Return last response (the failure) after exhausting retries
        return "POST".equals(method)
                ? delegate.post(url, body)
                : delegate.get(url);
    }
}
