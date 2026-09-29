/** Naive partition offset store: blindly accepts any commit, from any consumer, at any time. */
public class Partition {
    private int committedOffset;

    public Partition(int initialOffset) {
        this.committedOffset = initialOffset;
    }

    public void commit(int offset) {
        this.committedOffset = offset;
    }

    public int getCommittedOffset() {
        return committedOffset;
    }
}
