public class Main {
    static final int PRODUCTION_SCALE_ROWS = 2_000_000;

    public static void main(String[] args) {
        long start = System.nanoTime();
        FullParityStore fullParity = new FullParityStore();
        fullParity.provisionProductionScaleDataset(PRODUCTION_SCALE_ROWS);
        long provisionMillis = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Provisioning " + PRODUCTION_SCALE_ROWS + " rows into the full-parity store took "
            + provisionMillis + "ms on this run -- too slow to run on every local save, so team policy "
            + "reserves it for pre-merge CI, not day-to-day iteration.");

        System.out.println();
        System.out.println("Day-to-day local development uses the fallback store instead:");
        FallbackLocalStore fallback = new FallbackLocalStore();
        boolean firstCreate = fallback.create("Alice", "user1-data");
        boolean secondCreate = fallback.create("alice", "user2-data");

        System.out.println("Fallback store: create('Alice') succeeded: " + firstCreate);
        System.out.println("Fallback store: create('alice') succeeded: " + secondCreate);
        System.out.println("Fallback store accounts after both creates: " + fallback.size());

        System.out.println();
        System.out.println(secondCreate
            ? "CONFIRMED FAILURE: the FullParityStore's collation is genuinely correct -- if this exact "
              + "test had run against it, it would have caught the bug, exactly like the fix. But it's "
              + "too expensive to use for routine development, so the actual environment a developer "
              + "iterates in all day, every day, is the fallback store, which still has the original "
              + "case-sensitive bug. Parity that exists somewhere in the pipeline but isn't what "
              + "developers actually use minute to minute provides none of the benefit it was built for."
            : "Unexpected -- investigate.");
    }
}
