/**
 * Validates the bearer token and populates {@code context["authenticatedUser"]}.
 *
 * <p>Simplified simulation: a valid token starts with "Bearer " and is at least
 * 20 characters long.  A real implementation would verify a JWT signature.
 */
public final class AuthenticationHandler extends SecurityHandler {

    @Override
    public ApiResponse handle(ApiRequest request) {
        String token = request.getToken();

        if (token == null || !token.startsWith("Bearer ") || token.length() < 20) {
            System.out.printf("  [Authentication] FAIL     token=%s%n",
                    token == null ? "<null>" : token);
            return ApiResponse.unauthorized("Invalid or missing bearer token");
        }

        // Simulate extracting userId from the token payload
        String userId = request.getUserId().isEmpty()
                ? "user-" + token.substring(7, 12)
                : request.getUserId();

        request.getContext().put("authenticatedUser", userId);
        System.out.printf("  [Authentication] PASS     user=%s%n", userId);
        return passToNext(request);
    }
}
