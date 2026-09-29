/**
 * PRINCIPLE APPLIED: the server embeds both the price snapshot and the wall-clock timestamp it was
 * rendered at (RENDERED_AT_MS). The client's hydration logic derives the snapshot's actual age by
 * comparing that embedded timestamp against its own current wall-clock time at hydration
 * (hydratedAtMs - RENDERED_AT_MS), and only pays the re-fetch cost when that derived age exceeds the
 * business staleness tolerance -- otherwise it trusts the embedded snapshot and paints immediately.
 * (This still models the client's own current time as a supplied value rather than a real system
 * clock read, and assumes that value is accurate; a real implementation depends on the client's
 * wall clock being reasonably correct relative to whatever clock produced RENDERED_AT_MS, which is
 * an operational dependency this model does not itself verify.) Same two scenarios as before: a
 * fresh page skips the re-fetch entirely (full SSR fast-paint benefit retained), and a stale cached
 * page triggers exactly the targeted re-fetch it actually needs (correctness restored only where
 * it's actually required).
 */
public class Main {
    private static final long RENDERED_AT_MS = 0L; // wall-clock time embedded in the page at render
    private static final long PRICE_CHANGE_AT_MS = 300_000L;
    private static final int RENDERED_PRICE_CENTS = 5000;
    private static final int UPDATED_PRICE_CENTS = 5500;
    private static final long STALENESS_THRESHOLD_MS = 60_000L;
    private static final long REFETCH_LATENCY_MS = 180L;

    public static void main(String[] args) {
        runScenario("Scenario A: page served immediately after render", 0L);
        System.out.println();
        runScenario("Scenario B: page served from a 10-minute-old CDN cache", 600_000L);
    }

    private static void runScenario(String label, long hydratedAtMs) {
        long snapshotAgeMs = hydratedAtMs - RENDERED_AT_MS; // age derived from the embedded render timestamp
        int actualCurrentPriceCents = (hydratedAtMs >= PRICE_CHANGE_AT_MS) ? UPDATED_PRICE_CENTS : RENDERED_PRICE_CENTS;

        System.out.println(label + " (embedded snapshot age: " + snapshotAgeMs + "ms):");
        System.out.println("  Embedded (render-time) price: $" + centsToDollars(RENDERED_PRICE_CENTS));

        boolean isStale = snapshotAgeMs > STALENESS_THRESHOLD_MS;
        if (isStale) {
            System.out.println("  Hydration logic: snapshot age " + snapshotAgeMs + "ms exceeds the "
                    + STALENESS_THRESHOLD_MS + "ms tolerance -- re-fetch just the price");
            System.out.println("  Re-fetch cost paid: " + REFETCH_LATENCY_MS + "ms");
            System.out.println("  Displayed price: $" + centsToDollars(actualCurrentPriceCents) + " (freshly fetched)");
            System.out.println("  Result: correct price shown, and the " + REFETCH_LATENCY_MS
                    + "ms cost was only paid because the snapshot actually needed re-validating.");
        } else {
            System.out.println("  Hydration logic: snapshot age " + snapshotAgeMs + "ms is within the "
                    + STALENESS_THRESHOLD_MS + "ms tolerance -- trust embedded snapshot, no re-fetch");
            System.out.println("  Re-fetch cost paid: 0ms");
            System.out.println("  Displayed price: $" + centsToDollars(RENDERED_PRICE_CENTS) + " (embedded snapshot)");
            System.out.println("  Result: full SSR fast-paint benefit retained -- no network call was needed to "
                    + "confirm what was already known to be within tolerance.");
        }
    }

    private static String centsToDollars(int cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }
}
