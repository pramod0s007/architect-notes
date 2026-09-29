/**
 * Demonstrates that invalidating a negative cache entry as part of the
 * write path that creates the record gets both properties at once: the
 * same backend-load efficiency as unbounded negative caching, and zero
 * stale "not found" responses once the record actually exists.
 */
public class Main {
    public static void main(String[] args) {
        Backend backend = new Backend();
        NegativeCache cache = new NegativeCache(backend);

        // The same 100 lookups as the earlier scenarios.
        int totalLookups = 100;
        for (int i = 0; i < totalLookups; i++) {
            cache.lookup("user-999");
        }
        System.out.println("Lookups for a nonexistent key: " + totalLookups);
        System.out.println("Backend calls made: " + backend.lookupCount());

        // The user finishes signing up. Creation goes through the cache,
        // which invalidates the negative entry as part of the same call.
        cache.create("user-999", "Alice");
        System.out.println("Record created via cache.create(): user-999 -> Alice");

        // Ten legitimate lookups happen after the record was created.
        int lookupsAfterCreate = 10;
        int staleNotFound = 0;
        int foundAlice = 0;
        for (int i = 0; i < lookupsAfterCreate; i++) {
            String value = cache.lookup("user-999");
            if (value == null) {
                staleNotFound++;
            } else if (value.equals("Alice")) {
                foundAlice++;
            }
        }
        System.out.println("Lookups after record was created: " + lookupsAfterCreate
                + ", still returning stale 'not found': " + staleNotFound
                + ", correctly returning Alice: " + foundAlice);
    }
}
