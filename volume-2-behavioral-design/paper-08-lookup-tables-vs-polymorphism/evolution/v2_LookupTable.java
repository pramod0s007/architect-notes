// v2_LookupTable.java
// Fix: routes are data, not code.
// Adding a new route = one line in the registration block.
// The routing algorithm never changes.

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class v2_LookupTable {

    static class HttpRequest {
        final String method;
        final String path;
        HttpRequest(String method, String path) {
            this.method = method;
            this.path   = path;
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

    // -------------------------------------------------------------------------
    // Route key: "METHOD /path"  → simple, unambiguous lookup key
    // -------------------------------------------------------------------------

    static String routeKey(String method, String path) {
        return method.toUpperCase() + " " + path;
    }

    // -------------------------------------------------------------------------
    // The router: one Map, one lookup, zero branching.
    // -------------------------------------------------------------------------

    static class HttpRouter {
        // Routes are data. The routing algorithm is just map.getOrDefault().
        private final Map<String, Function<HttpRequest, HttpResponse>> routes = new HashMap<>();

        HttpRouter register(String method, String path,
                            Function<HttpRequest, HttpResponse> handler) {
            routes.put(routeKey(method, path), handler);
            return this;
        }

        HttpResponse route(HttpRequest request) {
            String key = routeKey(request.method, request.path);
            Function<HttpRequest, HttpResponse> handler = routes.get(key);
            if (handler == null) {
                return new HttpResponse(404, "No route for: " + key);
            }
            return handler.apply(request);
        }
    }

    // -------------------------------------------------------------------------
    // Handler logic kept in focused static methods — easy to find and test.
    // -------------------------------------------------------------------------

    static HttpResponse listUsers(HttpRequest req)        { return new HttpResponse(200, "List of all users"); }
    static HttpResponse getUserProfile(HttpRequest req)   { return new HttpResponse(200, "User profile data"); }
    static HttpResponse listProducts(HttpRequest req)     { return new HttpResponse(200, "Product catalog"); }
    static HttpResponse listOrders(HttpRequest req)       { return new HttpResponse(200, "Order history"); }
    static HttpResponse createUser(HttpRequest req)       { return new HttpResponse(201, "User created"); }
    static HttpResponse createProduct(HttpRequest req)    { return new HttpResponse(201, "Product created"); }
    static HttpResponse placeOrder(HttpRequest req)       { return new HttpResponse(201, "Order placed"); }
    static HttpResponse updateProfile(HttpRequest req)    { return new HttpResponse(200, "Profile updated"); }
    static HttpResponse updateProduct(HttpRequest req)    { return new HttpResponse(200, "Product updated"); }
    static HttpResponse deleteUser(HttpRequest req)       { return new HttpResponse(200, "User deleted"); }
    static HttpResponse cancelOrder(HttpRequest req)      { return new HttpResponse(200, "Order cancelled"); }

    // NEW: adding a report route = one register() call, zero edits to routing logic
    static HttpResponse monthlyReport(HttpRequest req)    { return new HttpResponse(200, "Monthly report data"); }

    public static void main(String[] args) {
        HttpRouter router = new HttpRouter()
            .register("GET",    "/users",           v2_LookupTable::listUsers)
            .register("GET",    "/users/profile",   v2_LookupTable::getUserProfile)
            .register("GET",    "/products",        v2_LookupTable::listProducts)
            .register("GET",    "/orders",          v2_LookupTable::listOrders)
            .register("POST",   "/users",           v2_LookupTable::createUser)
            .register("POST",   "/products",        v2_LookupTable::createProduct)
            .register("POST",   "/orders",          v2_LookupTable::placeOrder)
            .register("PUT",    "/users/profile",   v2_LookupTable::updateProfile)
            .register("PUT",    "/products",        v2_LookupTable::updateProduct)
            .register("DELETE", "/users",           v2_LookupTable::deleteUser)
            .register("DELETE", "/orders",          v2_LookupTable::cancelOrder)
            // Adding a new route: one line, no editing existing code
            .register("GET",    "/reports/monthly", v2_LookupTable::monthlyReport);

        HttpRequest[] requests = {
            new HttpRequest("GET",    "/users"),
            new HttpRequest("POST",   "/orders"),
            new HttpRequest("PUT",    "/users/profile"),
            new HttpRequest("DELETE", "/users"),
            new HttpRequest("GET",    "/reports/monthly"),
            new HttpRequest("GET",    "/unknown"),
        };

        for (HttpRequest req : requests) {
            System.out.printf("%-8s %-25s -> %s%n",
                req.method, req.path, router.route(req));
        }

        System.out.println("\nRoute table has " + 12 + " entries — adding more requires zero logic changes.");
    }
}
