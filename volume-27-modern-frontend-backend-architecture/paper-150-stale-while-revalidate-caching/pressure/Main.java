/**
 * PRESSURE: a client-side cache that serves a cached value forever and never revalidates it in the
 * background at all. This is a real, common shortcut -- "we cache it, it's fast, don't touch it" --
 * and it is fast, on every single read. But the price actually changed on the server after the value
 * was cached, and because nothing ever triggers a background revalidation, the client has no path back
 * to correctness: it will keep serving the original cached value indefinitely, no matter how much time
 * passes or how many reads happen, until the process restarts and the cache is empty again.
 */
public class Main {
    private static Integer cachedPriceCents = null;

    public static void main(String[] args) {
        System.out.println("Read 1 (cache empty): fetching from server...");
        cachedPriceCents = fetchFromServer();
        System.out.println("Displayed price: " + cachedPriceCents + " cents (freshly cached)");

        System.out.println("Server-side price change happens here: 1999 -> 2499");

        System.out.println("Read 2 (some time later): cache is populated, serving it directly, no revalidation");
        System.out.println("Displayed price: " + cachedPriceCents + " cents");

        System.out.println("Read 3 (much later still): cache is populated, serving it directly, no revalidation");
        System.out.println("Displayed price: " + cachedPriceCents + " cents");

        int actualCurrentPriceCents = 2499;
        System.out.println("Actual current server price: " + actualCurrentPriceCents + " cents");
        System.out.println("Result: displayed price (" + cachedPriceCents + ") never converges on the actual "
                + "current price (" + actualCurrentPriceCents + ") -- the cache has no mechanism to ever "
                + "revalidate itself, so it is permanently wrong from the moment the price changed onward.");
    }

    private static int fetchFromServer() {
        return 1999;
    }
}
