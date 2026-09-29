/**
 * Demonstrates pool exhaustion caused by a connection leak: request
 * code acquires a connection to do its work but never releases it
 * back to the pool afterward (no try/finally). Every one of the
 * pool's 5 connections gets handed out exactly once and never
 * returned, so a request that comes after all 5 are gone finds the
 * pool empty despite having only ever served 5 requests.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ConnectionPool pool = new ConnectionPool();
        int leaked = 0;

        for (int i = 0; i < ConnectionPool.POOL_SIZE; i++) {
            Connection conn = pool.acquire(200);
            doWork(conn);
            // BUG: no release(conn) call here -- the connection is never returned to the pool.
            leaked++;
        }

        System.out.println("Connections leaked (never released): " + leaked);
        System.out.println("Connections available in pool: " + pool.availableCount());

        long start = System.currentTimeMillis();
        Connection conn = pool.acquire(200);
        long waitedMs = System.currentTimeMillis() - start;

        System.out.println("Next request's acquire() result: "
                + (conn == null ? "timed out, no connection available" : "got connection " + conn.id()));
        System.out.println("That request waited approximately: " + waitedMs + "ms");
        System.out.println("Pool exhausted despite only " + leaked + " requests ever being served: " + (conn == null));
    }

    private static void doWork(Connection conn) {
        // Simulated request work using the connection.
    }
}
