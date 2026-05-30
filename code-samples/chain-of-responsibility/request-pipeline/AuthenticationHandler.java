public final class AuthenticationHandler extends Handler {

    @Override
    protected void process(Request request, PipelineContext context) {
        if (request.token() == null || request.token().isBlank()) {
            reject(context, "missing token");
            return;
        }
        pass(request, context);
    }

    @Override
    protected String stageName() {
        return "Authentication";
    }
}
