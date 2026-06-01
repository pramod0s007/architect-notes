/**
 * Abstract handler in the security pipeline chain.
 *
 * <p>Each concrete handler either:
 * <ul>
 *   <li>Short-circuits by returning a non-null {@link ApiResponse}, or
 *   <li>Passes the request to the next handler via {@link #passToNext(ApiRequest)}.
 * </ul>
 */
public abstract class SecurityHandler {

    private SecurityHandler next;

    /**
     * Appends {@code handler} at the end of the chain and returns it,
     * enabling fluent builder calls:
     * <pre>
     *   first.setNext(second).setNext(third)
     * </pre>
     */
    public SecurityHandler setNext(SecurityHandler handler) {
        this.next = handler;
        return handler;
    }

    /** Entry point — every subclass must implement its check here. */
    public abstract ApiResponse handle(ApiRequest request);

    /**
     * Delegates to the next handler, or returns 200 OK if this is the
     * last handler in the chain (all checks passed).
     */
    protected ApiResponse passToNext(ApiRequest request) {
        if (next != null) {
            return next.handle(request);
        }
        return ApiResponse.ok("Request processed successfully");
    }
}
