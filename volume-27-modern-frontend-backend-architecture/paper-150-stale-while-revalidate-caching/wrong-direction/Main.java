/**
 * WRONG DIRECTION: overcorrecting from "never revalidate" to "always revalidate synchronously before
 * showing anything, and if that fetch fails, show nothing at all." Every single read now blocks on a
 * live network call to the server, even reads that happen milliseconds apart, defeating the entire
 * point of caching. Worse: when the server has a transient failure, this design has no fallback path
 * to the last known-good cached value -- a single failed revalidation takes the display down entirely,
 * even though a perfectly good, only-slightly-stale cached value was sitting right there unused.
 */
public class Main {
    private static Integer cachedPriceCents = null;

    public static void main(String[] args) {
        System.out.println("Read 1 (cache empty): synchronously fetching from server (blocking)...");
        cachedPriceCents = fetchFromServer(false);
        System.out.println("Displayed price: " + cachedPriceCents + " cents (fetch succeeded, blocked on it)");

        System.out.println("Read 2 (milliseconds later): synchronously fetching from server AGAIN (blocking)...");
        cachedPriceCents = fetchFromServer(false);
        System.out.println("Displayed price: " + cachedPriceCents + " cents (fetch succeeded, blocked on it "
                + "again, even though the last read was milliseconds ago)");

        System.out.println("Read 3: server has a transient failure this time -- synchronously fetching (blocking)...");
        Integer freshValue = fetchFromServer(true);

        if (freshValue == null) {
            System.out.println("Fetch failed. This design has no fallback to the last known-good cached value ("
                    + cachedPriceCents + " cents) -- it discards the perfectly good cached value and shows "
                    + "nothing instead.");
            cachedPriceCents = null;
        }

        System.out.println("Displayed price: " + (cachedPriceCents == null ? "(nothing -- display is broken)"
                : cachedPriceCents + " cents"));
        System.out.println("Result: a single transient revalidation failure took the display down entirely, "
                + "even though a valid cached price (1999 cents) existed and could have been shown instead.");
    }

    private static Integer fetchFromServer(boolean simulateFailure) {
        if (simulateFailure) {
            return null;
        }
        return 1999;
    }
}
