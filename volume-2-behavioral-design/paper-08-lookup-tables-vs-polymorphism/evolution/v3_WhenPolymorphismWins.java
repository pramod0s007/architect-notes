// v3_WhenPolymorphismWins.java
// Shows WHERE the lookup table breaks down and polymorphism wins:
//   1. Handlers need shared behavior (auth check, rate limiting, logging)
//   2. Multiple operations on the same handler type (handle, canAccess, describe)
//   3. Type safety: compiler catches missing operations, not a runtime 404

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class v3_WhenPolymorphismWins {

    static class HttpRequest {
        final String method;
        final String path;
        final String authToken;
        HttpRequest(String method, String path, String authToken) {
            this.method    = method;
            this.path      = path;
            this.authToken = authToken;
        }
    }

    static class HttpResponse {
        final int    statusCode;
        final String body;
        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body       = body;
        }
        @Override public String toString() { return statusCode + " | " + body; }
    }

    // ─── Where the lookup table breaks down ───────────────────────────────────
    // Suppose every handler needs: authenticate, rate-limit check, handle, audit.
    // With lambdas in a Map you can't express that contract.
    // Each lambda is a Function<Request, Response> — nothing else.
    // To add a second operation (e.g., "describe this route") you need a second Map.
    // A third operation = a third Map. The maps go out of sync silently.

    // ─── Polymorphism: each handler is a class that owns its own behaviors ────

    interface RouteHandler {
        /** Primary: process the request */
        HttpResponse handle(HttpRequest request);

        /** Secondary: does this handler require authentication? */
        boolean requiresAuth();

        /** Tertiary: human-readable description for API docs */
        String describe();
    }

    // Shared auth logic lives in a base class — impossible with a lambda Map.
    static abstract class AuthenticatedHandler implements RouteHandler {
        @Override
        public boolean requiresAuth() { return true; }

        @Override
        public HttpResponse handle(HttpRequest request) {
            if (request.authToken == null || request.authToken.isEmpty()) {
                return new HttpResponse(401, "Unauthorized — token required");
            }
            return handleAuthenticated(request);
        }

        protected abstract HttpResponse handleAuthenticated(HttpRequest request);
    }

    static abstract class PublicHandler implements RouteHandler {
        @Override
        public boolean requiresAuth() { return false; }
    }

    // --- Concrete handlers ---

    static class ListUsersHandler extends AuthenticatedHandler {
        @Override
        protected HttpResponse handleAuthenticated(HttpRequest req) {
            return new HttpResponse(200, "List of users (auth OK)");
        }
        @Override public String describe() { return "GET /users — returns paginated user list, requires Bearer token"; }
    }

    static class GetProductsHandler extends PublicHandler {
        @Override
        public HttpResponse handle(HttpRequest req) {
            return new HttpResponse(200, "Public product catalog");
        }
        @Override public String describe() { return "GET /products — public product catalog, no auth required"; }
    }

    static class CreateOrderHandler extends AuthenticatedHandler {
        @Override
        protected HttpResponse handleAuthenticated(HttpRequest req) {
            return new HttpResponse(201, "Order placed for authenticated user");
        }
        @Override public String describe() { return "POST /orders — places a new order, requires Bearer token"; }
    }

    static class MonthlyReportHandler extends AuthenticatedHandler {
        @Override
        protected HttpResponse handleAuthenticated(HttpRequest req) {
            return new HttpResponse(200, "Monthly financial report (auth OK)");
        }
        @Override public String describe() { return "GET /reports/monthly — finance report, requires Bearer token"; }
    }

    // ─── Router: still uses a Map for O(1) lookup — but values are typed ─────

    static class HttpRouter {
        private final Map<String, RouteHandler> routes = new HashMap<>();

        HttpRouter register(String method, String path, RouteHandler handler) {
            routes.put(method.toUpperCase() + " " + path, handler);
            return this;
        }

        HttpResponse route(HttpRequest request) {
            String key = request.method.toUpperCase() + " " + request.path;
            RouteHandler handler = routes.get(key);
            if (handler == null) {
                return new HttpResponse(404, "No route: " + key);
            }
            return handler.handle(request);
        }

        void printApiDocs() {
            System.out.println("\n=== API Documentation ===");
            routes.forEach((key, h) ->
                System.out.printf("  [%s] %s%n", h.requiresAuth() ? "AUTH" : "PUBLIC", h.describe())
            );
        }
    }

    public static void main(String[] args) {
        HttpRouter router = new HttpRouter()
            .register("GET",  "/users",           new ListUsersHandler())
            .register("GET",  "/products",        new GetProductsHandler())
            .register("POST", "/orders",          new CreateOrderHandler())
            .register("GET",  "/reports/monthly", new MonthlyReportHandler());

        // Operation: handle
        HttpRequest[] requests = {
            new HttpRequest("GET",  "/users",           "token-abc"),
            new HttpRequest("GET",  "/users",           ""),           // missing auth
            new HttpRequest("GET",  "/products",        ""),           // public — no auth needed
            new HttpRequest("POST", "/orders",          "token-xyz"),
            new HttpRequest("GET",  "/reports/monthly", "token-abc"),
        };

        System.out.println("=== Request Routing ===");
        for (HttpRequest req : requests) {
            System.out.printf("%-8s %-25s auth=%-12s -> %s%n",
                req.method, req.path,
                req.authToken.isEmpty() ? "(none)" : req.authToken,
                router.route(req));
        }

        // Operation: generate API docs — impossible with a lambda Map
        router.printApiDocs();

        System.out.println("\nKey insight: when handlers share behavior (auth) or need multiple");
        System.out.println("operations (handle + describe + requiresAuth), a lambda Map runs dry.");
        System.out.println("Polymorphism lets you add operations without touching the router.");
    }
}
