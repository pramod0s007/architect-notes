/**
 * PRESSURE: a server-rendered page embeds a price snapshot and the timestamp it was rendered at.
 * The backend price changes 5 minutes after that render. The client's hydration logic simply
 * trusts the embedded snapshot forever -- it never computes how much wall-clock time has passed
 * since the render, so it has no way to tell a run where the price hasn't changed yet from a run
 * where it has. Two runs of the same page: one served immediately (hydrating before the price
 * change), and one served from a CDN edge cache after sitting there for 10 minutes (hydrating well
 * after the price change).
 */
public class Main {
    private static final long RENDERED_AT_MS = 0L;             // wall-clock time embedded in the page at render
    private static final long PRICE_CHANGE_AT_MS = 300_000L;   // backend price changes 5 minutes after render
    private static final int RENDERED_PRICE_CENTS = 5000;      // price baked into the HTML at render time
    private static final int UPDATED_PRICE_CENTS = 5500;       // what the backend price becomes after the change
    private static final long STALENESS_THRESHOLD_MS = 60_000L; // business tolerance: 1 minute

    public static void main(String[] args) {
        runScenario("Scenario A: page served immediately after render", 0L);
        System.out.println();
        runScenario("Scenario B: page served from a 10-minute-old CDN cache", 600_000L);
    }

    private static void runScenario(String label, long hydratedAtMs) {
        long snapshotAgeMs = hydratedAtMs - RENDERED_AT_MS;
        int actualCurrentPriceCents = (hydratedAtMs >= PRICE_CHANGE_AT_MS) ? UPDATED_PRICE_CENTS : RENDERED_PRICE_CENTS;

        System.out.println(label + " (embedded snapshot age: " + snapshotAgeMs + "ms):");
        System.out.println("  Embedded (render-time) price: $" + centsToDollars(RENDERED_PRICE_CENTS));
        System.out.println("  Hydration logic: trust embedded snapshot, never re-check");
        System.out.println("  Displayed price: $" + centsToDollars(RENDERED_PRICE_CENTS));

        boolean isStale = snapshotAgeMs > STALENESS_THRESHOLD_MS;
        boolean isWrong = RENDERED_PRICE_CENTS != actualCurrentPriceCents;
        if (isStale && isWrong) {
            System.out.println("  Result: snapshot is " + snapshotAgeMs + "ms old (past the " + STALENESS_THRESHOLD_MS
                    + "ms tolerance) and the actual current price is $" + centsToDollars(actualCurrentPriceCents)
                    + " -- user is shown a stale, incorrect price with no indication anything changed.");
        } else {
            System.out.println("  Result: snapshot is " + snapshotAgeMs
                    + "ms old and the price hasn't changed yet -- correct this run only because nothing had "
                    + "changed by " + snapshotAgeMs + "ms, not because the logic ever checked.");
        }
    }

    private static String centsToDollars(int cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }
}
