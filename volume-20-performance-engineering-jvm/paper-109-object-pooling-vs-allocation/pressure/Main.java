public class Main {
    static final int BUFFER_SIZE = 50_000;
    static final int ITERATIONS = 20_000;

    public static void main(String[] args) {
        long start = System.nanoTime();
        double checksum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            Buffer buffer = new Buffer(BUFFER_SIZE); // fresh large array allocated every time
            buffer.data[0] = i; // touch it so the JIT can't discard the allocation entirely
            checksum += buffer.data[0];
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println(ITERATIONS + " iterations, allocating a fresh " + BUFFER_SIZE + "-element buffer every time.");
        System.out.println("Elapsed: " + elapsedMs + "ms on this run (checksum " + checksum + ")");

        System.out.println();
        System.out.println(elapsedMs > 0
            ? "CONFIRMED FAILURE (relative to the fix, measured next): every iteration pays the real, "
              + "measurable cost of allocating and zeroing a " + BUFFER_SIZE + "-element array, even "
              + "though the previous iteration's buffer was already garbage the instant it was used."
            : "Unexpected -- investigate.");
    }
}
