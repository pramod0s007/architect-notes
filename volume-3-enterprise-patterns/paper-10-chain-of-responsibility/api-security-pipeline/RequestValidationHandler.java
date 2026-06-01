/**
 * Last stage: validates request body constraints.
 *
 * <p>POST and PUT requests must carry a non-empty body.
 * Other HTTP methods pass through unconditionally.
 */
public final class RequestValidationHandler extends SecurityHandler {

    @Override
    public ApiResponse handle(ApiRequest request) {
        String method = request.getMethod().toUpperCase();
        String body   = request.getBody();

        if (("POST".equals(method) || "PUT".equals(method))
                && (body == null || body.isBlank())) {
            System.out.printf("  [Validation]     FAIL     method=%s body=<empty>%n", method);
            return ApiResponse.badRequest("Request body must not be empty for " + method);
        }

        System.out.printf("  [Validation]     PASS     method=%s%n", method);
        return passToNext(request);
    }
}
