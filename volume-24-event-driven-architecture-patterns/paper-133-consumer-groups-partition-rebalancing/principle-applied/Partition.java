/**
 * A partition offset store that fences commits by generation ("epoch"). Every rebalance
 * bumps the epoch; a commit is only accepted if it carries the CURRENT epoch. A consumer
 * that was revoked before it learns about it ("a zombie") still holds the OLD epoch, so
 * its commit is rejected -- it can never push the committed offset past what the current
 * owner has actually confirmed.
 */
public class Partition {
    private int committedOffset;
    private int currentEpoch;

    public Partition(int initialOffset, int initialEpoch) {
        this.committedOffset = initialOffset;
        this.currentEpoch = initialEpoch;
    }

    /** Called by the group coordinator whenever a rebalance reassigns this partition. */
    public void startNewGeneration() {
        currentEpoch++;
    }

    /** Returns true if the commit was accepted, false if it was fenced (stale epoch). */
    public boolean commit(int offset, int epoch) {
        if (epoch != currentEpoch) {
            return false;
        }
        committedOffset = offset;
        return true;
    }

    public int getCommittedOffset() {
        return committedOffset;
    }

    public int getCurrentEpoch() {
        return currentEpoch;
    }
}
