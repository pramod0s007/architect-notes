import java.util.Map;
import java.util.Set;

public final class AuthorizationHandler extends Handler {

    private static final Map<String, Set<String>> PATH_ROLES = Map.of(
            "/orders", Set.of("customer", "admin"),
            "/admin/reports", Set.of("admin"));

    @Override
    protected void process(Request request, PipelineContext context) {
        Set<String> allowed = PATH_ROLES.get(request.path());
        if (allowed == null) {
            reject(context, "unknown path " + request.path());
            return;
        }
        String role = roleFor(request.userId());
        if (!allowed.contains(role)) {
            reject(context, "role " + role + " cannot access " + request.path());
            return;
        }
        pass(request, context);
    }

    private static String roleFor(String userId) {
        if (userId.startsWith("admin:")) {
            return "admin";
        }
        return "customer";
    }

    @Override
    protected String stageName() {
        return "Authorization";
    }
}
