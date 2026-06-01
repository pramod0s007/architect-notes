/**
 * Core HTTP client abstraction.
 *
 * Decorators wrap this interface to add cross-cutting concerns (retry,
 * logging, caching, circuit-breaking) without modifying the base contract.
 */
public interface HttpClient {

    /**
     * Perform an HTTP GET request.
     *
     * @param url the absolute URL to request
     * @return the server response
     */
    Response get(String url);

    /**
     * Perform an HTTP POST request.
     *
     * @param url  the absolute URL
     * @param body request body (typically JSON)
     * @return the server response
     */
    Response post(String url, String body);
}
