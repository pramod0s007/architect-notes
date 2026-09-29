/**
 * Demonstrates permanent staleness: the cache loads a value once, the
 * backing store is updated afterward (e.g. by an admin editing a record),
 * and the cache keeps serving the original value forever because nothing
 * ever tells it the data changed.
 */
public class Main {
    public static void main(String[] args) {
        BackingStore backingStore = new BackingStore("price:9.99");
        NaiveCache cache = new NaiveCache(backingStore);

        String first = cache.get("product:42");
        System.out.println("First read: " + first + " (backing store loads so far: "
                + backingStore.getLoadCount() + ")");

        // The underlying data changes -- a price update, applied directly to the store.
        backingStore.update("price:7.49");
        System.out.println("Backing store updated to price:7.49 (a real price drop)");

        String second = cache.get("product:42");
        System.out.println("Second read: " + second + " (backing store loads so far: "
                + backingStore.getLoadCount() + ")");

        String third = cache.get("product:42");
        System.out.println("Third read, much later: " + third + " (backing store loads so far: "
                + backingStore.getLoadCount() + ")");

        System.out.println();
        if (second.equals("price:9.99") && third.equals("price:9.99")) {
            System.out.println("CONFIRMED FAILURE: the cache is still serving price:9.99, the"
                    + " original value, even though the backing store has said price:7.49 for a"
                    + " while now. The cache never checked again -- it loaded once and never let go.");
        }
    }
}
