import java.util.HashMap;
import java.util.Map;

public final class RateLimitHandler extends Handler {

    private static final int MAX_PER_USER = 3;
    private final Map<String, Integer> counts = new HashMap<>();

    @Override
    protected void process(Request request, PipelineContext context) {
        int count = counts.merge(request.userId(), 1, Integer::sum);
        if (count > MAX_PER_USER) {
            reject(context, "rate limit exceeded for " + request.userId());
            return;
        }
        pass(request, context);
    }

    @Override
    protected String stageName() {
        return "Rate Limiting";
    }
}
