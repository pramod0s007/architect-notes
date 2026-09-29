/**
 * Demonstrates that memoizing negative lookups forever genuinely fixes the
 * backend-load problem, but poisons the cache the moment the missing
 * record is later created: nothing tells the cache the key now exists,
 * so it keeps serving "not found" indefinitely, even after the real data
 * is there.
 */
public class Main {
    public static void main(String[] args) {
        Backend backend = new Backend();
        NegativeCache cache = new NegativeCache(backend);

        // The same 100 lookups as the pressure scenario, but through the
        // negative cache this time.
        int totalLookups = 100;
        for (int i = 0; i < totalLookups; i++) {
            cache.lookup("user-999");
        }
        System.out.println("Lookups for a nonexistent key: " + totalLookups);
        System.out.println("Backend calls made: " + backend.lookupCount());

        // The user finishes signing up -- the record now genuinely exists.
        backend.create("user-999", "Alice");
        System.out.println("Record created in backend: user-999 -> Alice");

        // Ten legitimate lookups happen after the record was created.
        int lookupsAfterCreate = 10;
        int staleNotFound = 0;
        for (int i = 0; i < lookupsAfterCreate; i++) {
            String value = cache.lookup("user-999");
            if (value == null) {
                staleNotFound++;
            }
        }
        System.out.println("Lookups after record was created: " + lookupsAfterCreate
                + ", still returning stale 'not found': " + staleNotFound);
        System.out.println("Backend's actual current record: " + backend.lookup("user-999"));
    }
}
