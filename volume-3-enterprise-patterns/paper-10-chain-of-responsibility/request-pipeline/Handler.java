public abstract class Handler {

    private Handler next;

    public Handler link(Handler next) {
        this.next = next;
        return next;
    }

    public final void handle(Request request, PipelineContext context) {
        if (context.failed()) {
            return;
        }
        process(request, context);
        if (!context.failed() && next != null) {
            next.handle(request, context);
        }
    }

    protected abstract void process(Request request, PipelineContext context);

    protected abstract String stageName();

    protected void pass(Request request, PipelineContext context) {
        context.record(stageName());
    }

    protected void reject(PipelineContext context, String reason) {
        context.fail(stageName(), reason);
    }

    public static final class PipelineContext {
        private final StringBuilder trace = new StringBuilder();
        private String failureStage;
        private String failureReason;

        void record(String stage) {
            if (!trace.isEmpty()) {
                trace.append('\n');
            }
            trace.append(stage);
        }

        void fail(String stage, String reason) {
            failureStage = stage;
            failureReason = reason;
            record(stage + " (rejected)");
        }

        boolean failed() {
            return failureStage != null;
        }

        String trace() {
            return trace.toString();
        }

        String failureStage() {
            return failureStage;
        }

        String failureReason() {
            return failureReason;
        }
    }
}
