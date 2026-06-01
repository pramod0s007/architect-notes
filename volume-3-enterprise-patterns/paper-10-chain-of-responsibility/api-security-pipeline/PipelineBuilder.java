/**
 * Fluent builder for assembling a {@link SecurityHandler} chain.
 *
 * <p>Usage:
 * <pre>
 *   SecurityHandler pipeline = new PipelineBuilder()
 *       .add(new IpBlocklistHandler(blockedIps))
 *       .add(new AuthenticationHandler())
 *       .add(new AuthorizationHandler(permissions))
 *       .add(new RateLimitHandler(100))
 *       .add(new RequestValidationHandler())
 *       .build();
 * </pre>
 */
public final class PipelineBuilder {

    private SecurityHandler head;
    private SecurityHandler tail;

    public PipelineBuilder add(SecurityHandler handler) {
        if (head == null) {
            head = handler;
            tail = handler;
        } else {
            tail.setNext(handler);
            tail = handler;
        }
        return this;
    }

    /**
     * Returns the head of the assembled chain.
     *
     * @throws IllegalStateException if no handlers have been added.
     */
    public SecurityHandler build() {
        if (head == null) {
            throw new IllegalStateException("Pipeline has no handlers");
        }
        return head;
    }
}
