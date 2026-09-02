// A primary database node. It has no idea whether a failover controller still
// considers it the primary -- it just keeps accepting writes from whichever
// clients can still reach it, on whatever network path THEY use, which may be
// completely different from the path a health checker uses.
class PrimaryNode {
    final String id;
    private boolean acceptingWrites = true;
    private String value;

    PrimaryNode(String id) {
        this.id = id;
    }

    boolean write(String newValue) {
        if (!acceptingWrites) {
            return false;
        }
        this.value = newValue;
        return true;
    }

    String currentValue() {
        return value;
    }

    void revokeWriteAccess() {
        acceptingWrites = false;
    }
}
