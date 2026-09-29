/**
 * WRONG DIRECTION: the client now does reconcile with server responses, applying each response to
 * the displayed count as it arrives -- but it does so unconditionally, in whatever order the
 * responses happen to arrive over the network, with no way to tell an old response from a newer
 * one. The response to request #2 (which reflects the up-to-date server state, including a
 * concurrent external like) arrives FIRST due to network reordering. The response to request #1
 * (an earlier, now-stale snapshot of the server state) arrives SECOND and overwrites the correct
 * value with a stale one.
 */
public class Main {
    private static final int BASE_COUNT = 10;

    public static void main(String[] args) {
        System.out.println("Base server count: " + BASE_COUNT);

        int localCount = BASE_COUNT;
        localCount += 1;
        System.out.println("User clicks like (1st time) -- optimistic local count: " + localCount);
        localCount += 1;
        System.out.println("User clicks like (2nd time) -- optimistic local count: " + localCount);

        // Response for request #2 arrives first (network reordering) -- reflects the up-to-date
        // server state, including the concurrent external like.
        int responseTwoCount = BASE_COUNT + 1 /* request 1 */ + 1 /* concurrent external like */ + 1 /* request 2 */;
        localCount = responseTwoCount;
        System.out.println("Response for request #2 arrives first: server count = " + responseTwoCount
                + " -- displayed count overwritten to " + localCount);

        // Response for request #1 arrives second -- a stale snapshot taken before the external like
        // and before request #2 had even been processed.
        int responseOneCount = BASE_COUNT + 1 /* request 1 only */;
        localCount = responseOneCount;
        System.out.println("Response for request #1 arrives second (out of order): server count = "
                + responseOneCount + " -- displayed count overwritten to " + localCount);

        System.out.println("Final displayed count: " + localCount);
        System.out.println("Actual current server count: " + responseTwoCount);

        if (localCount != responseTwoCount) {
            System.out.println("Result: the correct count (" + responseTwoCount + ") was briefly displayed, then "
                    + "overwritten by a stale, out-of-order response down to " + localCount + " -- unconditionally "
                    + "applying whichever response arrives last, rather than whichever request was sent last, "
                    + "regressed a correct value back to an incorrect one.");
        }
    }
}
