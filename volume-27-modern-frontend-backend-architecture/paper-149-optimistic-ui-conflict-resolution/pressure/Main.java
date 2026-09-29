/**
 * PRESSURE: a "like" button applies its increment to the local UI immediately (optimistic update),
 * for instant feedback, and never looks at the server's response at all -- the two "like" requests
 * are fired and their responses are simply discarded. Meanwhile, a different user likes the same
 * post concurrently, on the server side, in between this client's two requests. The server's true
 * count ends up higher than what this client's optimistic math alone could ever produce, and
 * because nothing ever reconciles against the server, the client never finds out.
 */
public class Main {
    private static final int BASE_COUNT = 10; // count before this client does anything

    public static void main(String[] args) {
        int localCount = BASE_COUNT;
        System.out.println("Base server count: " + BASE_COUNT);

        // Optimistic update #1: user clicks "like"
        localCount += 1;
        System.out.println("User clicks like (1st time) -- optimistic local count: " + localCount);

        // Optimistic update #2: user clicks "like" again
        localCount += 1;
        System.out.println("User clicks like (2nd time) -- optimistic local count: " + localCount);

        // Server-side reality: this client's 2 likes, PLUS a concurrent like from a different user,
        // land in between -- true server count ends up higher than pure local optimism accounts for.
        int trueServerCount = BASE_COUNT + 1 /* request 1 */ + 1 /* concurrent external like */ + 1 /* request 2 */;

        System.out.println("Server responses received: never read -- client keeps its own local count forever");
        System.out.println("Final displayed count: " + localCount);
        System.out.println("Actual server count (never reconciled): " + trueServerCount);

        if (localCount != trueServerCount) {
            System.out.println("Result: displayed count (" + localCount + ") permanently understates the true "
                    + "server count (" + trueServerCount + ") -- the concurrent external like is invisible to "
                    + "this client because it never looks at what the server actually says.");
        }
    }
}
