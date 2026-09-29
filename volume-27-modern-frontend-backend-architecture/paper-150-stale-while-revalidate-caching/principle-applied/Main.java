/**
 * PRINCIPLE APPLIED: stale-while-revalidate. Each read selects and displays whatever is currently
 * cached first, then a revalidation attempt runs afterward (modeled here as a plain synchronous call,
 * standing in for what a real system would run as a non-blocking background request). If the
 * revalidation succeeds, the cache is updated so the *next* read picks up the new value. If the
 * revalidation fails, the last known-good cached value is kept -- a transient failure degrades to
 * "possibly stale" instead of "broken," and it never loses a good value just because one revalidation
 * attempt failed.
 */
public class Main {
    private static Integer cachedPriceCents = null;
    private static boolean possiblyStale = false;

    public static void main(String[] args) {
        System.out.println("Read 1 (cache empty): must block once to populate the cache...");
        cachedPriceCents = fetchFromServer(false);
        System.out.println("Displayed price: " + cachedPriceCents + " cents (initial population)");

        System.out.println("Server-side price change happens here: 1999 -> 2499");

        System.out.println("Read 2: selecting and displaying the cached value first, "
                + "then attempting a modeled revalidation...");
        System.out.println("Displayed price: " + cachedPriceCents + " cents (from cache, before revalidation)");
        revalidateAfterRead(false);
        System.out.println("Revalidation succeeded -- cache updated to " + cachedPriceCents
                + " cents for the next read");

        System.out.println("Read 3: selecting and displaying the cached value first, "
                + "then attempting a modeled revalidation...");
        System.out.println("Displayed price: " + cachedPriceCents + " cents (from cache, reflecting the cache "
                + "update that followed Read 2's successful revalidation)");
        revalidateAfterRead(true);
        System.out.println("Revalidation failed (transient server error) -- keeping last "
                + "known-good cached value (" + cachedPriceCents + " cents) instead of discarding it, "
                + "flagged possibly-stale for observability: " + possiblyStale);

        System.out.println("Read 4: cache still holds the last known-good value, "
                + "selected and displayed despite the prior revalidation failure");
        System.out.println("Displayed price: " + cachedPriceCents + " cents");

        int actualCurrentPriceCents = 2499;
        System.out.println("Actual current server price: " + actualCurrentPriceCents + " cents");
        System.out.println("Result: no read after the first had to wait on its own revalidation attempt to be "
                + "displayed, the cache converged to the new price for the read that followed a successful "
                + "revalidation, and a later revalidation failure was survived without losing the last "
                + "known-good value or breaking the display.");
    }

    private static int fetchFromServer(boolean simulateFailure) {
        return 1999;
    }

    private static void revalidateAfterRead(boolean simulateFailure) {
        if (simulateFailure) {
            possiblyStale = true;
            return;
        }
        cachedPriceCents = 2499;
        possiblyStale = false;
    }
}
