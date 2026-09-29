/**
 * Demonstrates write-behind's real durability gap: a burst of writes is
 * accepted and acknowledged to the caller immediately, buffered only in
 * process memory, waiting for the next scheduled flush to make them
 * durable in the backend. If the process crashes before that scheduled
 * flush runs, every buffered write is gone -- not delayed, gone -- even
 * though the caller was already told each one succeeded.
 */
public class Main {
    private static final int WRITE_COUNT = 30;

    public static void main(String[] args) {
        Backend backend = new Backend();
        WriteBehindCache cache = new WriteBehindCache(backend);

        System.out.println("=== " + WRITE_COUNT + " writes accepted by the write-behind cache ===");
        for (int i = 0; i < WRITE_COUNT; i++) {
            cache.write("key-" + i, "value-" + i);
        }
        System.out.println("Writes acknowledged to caller: " + WRITE_COUNT);
        System.out.println("Writes actually durable in backend so far: " + backend.writeCount());

        // The process crashes here -- before the scheduled flush ever runs.
        System.out.println();
        System.out.println("=== Process crashes before the scheduled flush runs ===");
        System.out.println("Writes acknowledged to caller: " + WRITE_COUNT);
        System.out.println("Writes actually durable in backend after crash: " + backend.writeCount());
        int lost = WRITE_COUNT - backend.writeCount();

        if (lost == WRITE_COUNT) {
            System.out.println();
            System.out.println("CONFIRMED FAILURE: all " + WRITE_COUNT + " writes were acknowledged to the "
                    + "caller as successful, and all " + lost + " of them are gone. The write-behind cache's "
                    + "speed comes entirely from deferring the backend write to a scheduled flush, and a crash "
                    + "before that flush runs means the buffered writes never existed anywhere durable at all.");
        }
    }
}
