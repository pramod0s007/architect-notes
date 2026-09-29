/**
 * Demonstrates that without a pool or any admission control in the
 * application, a burst of requests each opening its own direct
 * connection runs straight into the database's own hard connection
 * limit -- with no queueing, no reuse, and no way for the application
 * to do anything but let most of the burst fail outright. The burst is
 * simulated here as a sequential loop (no real threads), since the
 * outcome depends only on how many connections the database has open
 * at once, not on the arrival ordering of the requests.
 */
public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        int requestCount = 12;
        int succeeded = 0;
        int refused = 0;

        // Represents a burst of 12 requests (e.g. a traffic spike),
        // each holding its own direct connection to the database for
        // the duration of the request. Simulated sequentially rather
        // than with real threads -- the database's hard limit rejects
        // overflow regardless of arrival ordering, so no concurrency
        // primitive is needed to demonstrate the failure.
        for (int i = 0; i < requestCount; i++) {
            try {
                db.connect();
                succeeded++;
            } catch (IllegalStateException e) {
                refused++;
            }
        }

        System.out.println("Requests in this burst: " + requestCount);
        System.out.println("Database max connections: " + Database.MAX_CONNECTIONS);
        System.out.println("Requests that got a connection: " + succeeded);
        System.out.println("Requests refused (no pooling or queueing at all): " + refused);
    }
}
