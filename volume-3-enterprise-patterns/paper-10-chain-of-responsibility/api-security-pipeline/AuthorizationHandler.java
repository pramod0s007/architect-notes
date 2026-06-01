import java.util.Map;
import java.util.Set;

/**
 * Checks whether the authenticated user has permission for the requested endpoint.
 *
 * <p>ACL is a simple map of userId → allowed endpoint prefixes.
 * A real system would query RBAC/ABAC policies.
 */
public final class AuthorizationHandler extends SecurityHandler {

    private final Map<String, Set<String>> permissions;

    public AuthorizationHandler(Map<String, Set<String>> permissions) {
        this.permissions = Map.copyOf(permissions);
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        String user     = (String) request.getContext().getOrDefault("authenticatedUser", "");
        String endpoint = request.getEndpoint();

        Set<String> allowed = permissions.getOrDefault(user, Set.of());
        boolean permitted = allowed.stream().anyMatch(endpoint::startsWith);

        if (!permitted) {
            System.out.printf("  [Authorization]  DENIED   user=%s endpoint=%s%n", user, endpoint);
            return ApiResponse.forbidden("User " + user + " is not authorized for " + endpoint);
        }

        System.out.printf("  [Authorization]  PASS     user=%s endpoint=%s%n", user, endpoint);
        return passToNext(request);
    }
}
