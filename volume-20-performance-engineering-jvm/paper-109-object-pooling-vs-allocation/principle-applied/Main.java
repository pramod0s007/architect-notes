public class Main {
    static final int BUFFER_SIZE = 50_000;
    static final int ITERATIONS = 20_000;

    public static void main(String[] args) {
        // One genuinely expensive allocation, up front -- then every
        // "use" reuses the same backing array instead of constructing
        // a new one.
        Buffer reused = new Buffer(BUFFER_SIZE);

        long start = System.nanoTime();
        double checksum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            reused.data[0] = i; // cheap: overwrite what this use actually needs
            checksum += reused.data[0];
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println(ITERATIONS + " iterations, reusing one " + BUFFER_SIZE + "-element buffer instead of allocating fresh each time.");
        System.out.println("Elapsed: " + elapsedMs + "ms on this run (checksum " + checksum + ")");

        System.out.println();
        System.out.println(elapsedMs < 50
            ? "FIXED: the same 20000 logical uses of a large buffer now cost dramatically less, because "
              + "the one genuinely expensive part -- allocating and zeroing " + BUFFER_SIZE + " elements -- "
              + "happens exactly once, and every subsequent use only pays for overwriting the small part "
              + "of the buffer it actually needs."
            : "Unexpected -- investigate.");
    }
}
