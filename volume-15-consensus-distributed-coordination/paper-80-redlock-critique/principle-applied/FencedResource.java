// The actual resource the lock is protecting. It never trusts "I hold the
// lock" on its own -- every write must carry a fencing token, a number that
// increases every time the lock is granted (by any means, to any client), and
// the resource rejects any write whose token is lower than the highest it has
// already accepted. Even if the LOCK layer is fooled by clock drift into
// letting two clients believe they hold it, the RESOURCE layer still can't be.
class FencedResource {
    private int highestTokenAccepted = 0;
    private String currentValue;

    boolean write(int fencingToken, String value) {
        if (fencingToken < highestTokenAccepted) {
            return false;
        }
        highestTokenAccepted = fencingToken;
        currentValue = value;
        return true;
    }

    String currentValue() {
        return currentValue;
    }
}
