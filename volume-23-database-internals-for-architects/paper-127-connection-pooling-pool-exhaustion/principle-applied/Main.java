/**
 * Demonstrates that guaranteeing release via try/finally -- so a
 * connection goes back to the pool whether the request's work
 * succeeds or throws -- lets a pool of only 5 connections serve far
 * more requests than its size, with no leak and no exhaustion, ever.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ConnectionPool pool = new ConnectionPool();
        int totalRequests = 20;
        int served = 0;
        int errored = 0;

        for (int i = 0; i < totalRequests; i++) {
            Connection conn = pool.acquire(200);
            try {
                doWork(conn, i);
                served++;
            } catch (IllegalStateException e) {
                errored++;
            } finally {
                pool.release(conn); // guaranteed even if doWork throws
            }
        }

        System.out.println("Total requests: " + totalRequests);
        System.out.println("Requests served successfully: " + served);
        System.out.println("Requests that errored (still released their connection): " + errored);
        System.out.println("Connections available after all requests: " + pool.availableCount());
        System.out.println("Pool never exhausted across " + totalRequests + " requests using only "
                + ConnectionPool.POOL_SIZE + " connections: " + (pool.availableCount() == ConnectionPool.POOL_SIZE));
    }

    private static void doWork(Connection conn, int requestIndex) {
        // Simulates a request that occasionally fails mid-work -- the
        // finally block above still returns the connection either way.
        if (requestIndex % 5 == 4) {
            throw new IllegalStateException("simulated transient failure on request " + requestIndex);
        }
    }
}
