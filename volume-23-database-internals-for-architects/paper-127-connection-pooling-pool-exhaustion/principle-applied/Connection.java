/** A pooled connection handle -- just enough identity to trace which one a request got. */
public class Connection {
    private final int id;

    public Connection(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}
