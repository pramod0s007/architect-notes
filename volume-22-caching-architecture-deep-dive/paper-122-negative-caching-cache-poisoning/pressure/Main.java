/**
 * Demonstrates the cost of having no negative caching at all: every lookup
 * for a key that doesn't exist hits the backend, even when the exact same
 * missing key is looked up repeatedly.
 */
public class Main {
    public static void main(String[] args) {
        Backend backend = new Backend();

        // A key that genuinely doesn't exist gets looked up 100 times --
        // e.g. a scanner probing IDs, or a client retrying a lookup for a
        // resource it doesn't realize is missing.
        int totalLookups = 100;
        int foundCount = 0;
        for (int i = 0; i < totalLookups; i++) {
            String value = backend.lookup("user-999");
            if (value != null) {
                foundCount++;
            }
        }

        System.out.println("Lookups for a nonexistent key: " + totalLookups);
        System.out.println("Backend calls made: " + backend.lookupCount());
        System.out.println("Found: " + foundCount);
    }
}
