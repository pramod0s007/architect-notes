// v1_IfElseRouter.java
// Problem: HTTP router with if-else chains for method + route.
// Adding a route means finding the right else-if block and inserting code.
// The route table is buried in procedural logic.

import java.util.HashMap;
import java.util.Map;

public class v1_IfElseRouter {

    static class HttpRequest {
        final String method;
        final String path;
        final Map<String, String> headers;
        final String body;

        HttpRequest(String method, String path) {
            this.method  = method;
            this.path    = path;
            this.headers = new HashMap<>();
            this.body    = "";
        }
    }

    static class HttpResponse {
        final int    statusCode;
        final String body;
        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body       = body;
        }
        @Override public String toString() {
            return statusCode + " | " + body;
        }
    }

    // -------------------------------------------------------------------------
    // The router: one giant method.
    // Adding "GET /reports/monthly" means editing this method and finding
    // the right place to insert — there is no obvious "right place".
    // -------------------------------------------------------------------------

    static class HttpRouter {

        HttpResponse route(HttpRequest request) {
            if (request.method.equals("GET")) {
                if (request.path.equals("/users")) {
                    return new HttpResponse(200, "List of all users");
                } else if (request.path.equals("/users/profile")) {
                    return new HttpResponse(200, "User profile data");
                } else if (request.path.equals("/products")) {
                    return new HttpResponse(200, "Product catalog");
                } else if (request.path.equals("/orders")) {
                    return new HttpResponse(200, "Order history");
                } else {
                    return new HttpResponse(404, "GET route not found: " + request.path);
                }
            } else if (request.method.equals("POST")) {
                if (request.path.equals("/users")) {
                    return new HttpResponse(201, "User created");
                } else if (request.path.equals("/products")) {
                    return new HttpResponse(201, "Product created");
                } else if (request.path.equals("/orders")) {
                    return new HttpResponse(201, "Order placed");
                } else {
                    return new HttpResponse(404, "POST route not found: " + request.path);
                }
            } else if (request.method.equals("PUT")) {
                if (request.path.equals("/users/profile")) {
                    return new HttpResponse(200, "Profile updated");
                } else if (request.path.equals("/products")) {
                    return new HttpResponse(200, "Product updated");
                } else {
                    return new HttpResponse(404, "PUT route not found: " + request.path);
                }
            } else if (request.method.equals("DELETE")) {
                if (request.path.equals("/users")) {
                    return new HttpResponse(200, "User deleted");
                } else if (request.path.equals("/orders")) {
                    return new HttpResponse(200, "Order cancelled");
                } else {
                    return new HttpResponse(404, "DELETE route not found: " + request.path);
                }
            }
            return new HttpResponse(405, "Method not allowed: " + request.method);
        }
    }

    public static void main(String[] args) {
        HttpRouter router = new HttpRouter();

        HttpRequest[] requests = {
            new HttpRequest("GET",    "/users"),
            new HttpRequest("POST",   "/orders"),
            new HttpRequest("PUT",    "/users/profile"),
            new HttpRequest("DELETE", "/users"),
            new HttpRequest("GET",    "/unknown"),
        };

        for (HttpRequest req : requests) {
            System.out.printf("%-8s %-20s -> %s%n",
                req.method, req.path, router.route(req));
        }

        System.out.println("\n--- Problem: adding GET /reports/monthly means editing the if-else above ---");
    }
}
