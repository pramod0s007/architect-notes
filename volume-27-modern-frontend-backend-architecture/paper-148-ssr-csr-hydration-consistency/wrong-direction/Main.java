/**
 * WRONG DIRECTION: to fix the staleness problem, the client's hydration logic now unconditionally
 * re-fetches the current price before displaying anything, on every single hydration, regardless of
 * how old the embedded snapshot actually is or whether the backend price has actually changed. This
 * is modeled as blocking the display of a price on the fetch completing, the same way the embedded
 * snapshot blocked nothing in the pressure scenario -- a real implementation could instead paint the
 * embedded snapshot immediately and revalidate in the background, which would avoid the specific
 * cost shown here at the price of its own added complexity (a moment where the displayed value can
 * visibly change after paint). This scenario does fix correctness -- the displayed price always
 * matches the backend -- but it pays the same fetch cost in the fresh run as in the stale run, even
 * though the fresh run's embedded snapshot hadn't gone stale yet and needed no correction at all.
 */
public class Main {
    private static final long RENDERED_AT_MS = 0L;
    private static final long PRICE_CHANGE_AT_MS = 300_000L;
    private static final int RENDERED_PRICE_CENTS = 5000;
    private static final int UPDATED_PRICE_CENTS = 5500;
    private static final long STALENESS_THRESHOLD_MS = 60_000L;
    private static final long REFETCH_LATENCY_MS = 180L; // cost of the unconditional, blocking re-fetch call

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
        System.out.println("  Hydration logic: always re-fetch current price before displaying it, regardless of snapshot age");
        System.out.println("  Re-fetch cost paid: " + REFETCH_LATENCY_MS + "ms");
        System.out.println("  Displayed price: $" + centsToDollars(actualCurrentPriceCents) + " (freshly fetched)");

        boolean wasActuallyStale = RENDERED_PRICE_CENTS != actualCurrentPriceCents;
        if (!wasActuallyStale) {
            System.out.println("  Result: correct price shown, but the " + REFETCH_LATENCY_MS
                    + "ms re-fetch cost was paid even though the embedded snapshot still matched reality -- the "
                    + "SSR fast-paint benefit was discarded for no correctness gain this run.");
        } else {
            System.out.println("  Result: correct price shown. The " + REFETCH_LATENCY_MS
                    + "ms re-fetch cost was necessary here, since the embedded snapshot no longer matched the "
                    + "actual price.");
        }
    }

    private static String centsToDollars(int cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }
}
