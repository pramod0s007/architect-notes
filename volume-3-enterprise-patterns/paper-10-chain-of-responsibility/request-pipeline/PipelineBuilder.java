public final class PipelineBuilder {

    public Handler build() {
        Handler authentication = new AuthenticationHandler();
        Handler authorization = new AuthorizationHandler();
        Handler validation = new ValidationHandler();
        Handler rateLimit = new RateLimitHandler();

        authentication.link(authorization).link(validation).link(rateLimit);
        return authentication;
    }

    public PipelineResult run(Request request) {
        Handler.PipelineContext context = new Handler.PipelineContext();
        build().handle(request, context);
        return new PipelineResult(context);
    }

    public static final class PipelineResult {
        private final Handler.PipelineContext context;

        PipelineResult(Handler.PipelineContext context) {
            this.context = context;
        }

        public boolean success() {
            return !context.failed();
        }

        public String trace() {
            return context.trace();
        }

        public String failureReason() {
            return context.failureReason();
        }
    }
}
