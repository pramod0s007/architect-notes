/**
 * Simulates a durable backend datastore. Every successful write is
 * recorded permanently -- this models the ground truth of what actually
 * survives, regardless of what the cache believes it has accepted.
 */
public class Backend {
    private int writeCount = 0;

    public void write(String key, String value) {
        writeCount++;
    }

    public int writeCount() {
        return writeCount;
    }
}
