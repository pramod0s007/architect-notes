/**
 * A database with a hard connection limit, exactly like a real
 * database server enforces (e.g. Postgres's max_connections). There
 * is no pool or admission control layer in front of it -- every
 * request opens its own direct connection, and the database itself
 * refuses outright once it's at capacity.
 */
public class Database {
    public static final int MAX_CONNECTIONS = 5;

    private int activeConnections = 0;

    public void connect() {
        if (activeConnections >= MAX_CONNECTIONS) {
            throw new IllegalStateException(
                    "Connection refused: database at max connections (" + MAX_CONNECTIONS + ")");
        }
        activeConnections++;
    }

    public void disconnect() {
        activeConnections--;
    }

    public int activeConnections() {
        return activeConnections;
    }
}
