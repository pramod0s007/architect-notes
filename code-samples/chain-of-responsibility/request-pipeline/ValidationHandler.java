public final class ValidationHandler extends Handler {

    @Override
    protected void process(Request request, PipelineContext context) {
        if ("POST".equalsIgnoreCase(request.method())
                && (request.body() == null || request.body().isBlank())) {
            reject(context, "POST requires a body");
            return;
        }
        pass(request, context);
    }

    @Override
    protected String stageName() {
        return "Validation";
    }
}
