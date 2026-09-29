import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A fixed-size connection pool: a bounded set of connections that
 * requests borrow and are expected to return. Acquiring blocks up to
 * a timeout waiting for one to become available, rather than either
 * refusing outright or waiting forever.
 */
public class ConnectionPool {
    public static final int POOL_SIZE = 5;

    private final ArrayBlockingQueue<Connection> available = new ArrayBlockingQueue<>(POOL_SIZE);

    public ConnectionPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            available.add(new Connection(i));
        }
    }

    /** Waits up to timeoutMs for an available connection; returns null if none becomes available in time. */
    public Connection acquire(long timeoutMs) throws InterruptedException {
        return available.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public void release(Connection conn) {
        available.offer(conn);
    }

    public int availableCount() {
        return available.size();
    }
}
