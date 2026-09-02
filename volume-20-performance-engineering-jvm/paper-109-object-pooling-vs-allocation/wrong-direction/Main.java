public class Main {
    static final int ITERATIONS = 20_000_000;

    public static void main(String[] args) {
        long checksum = 0;

        long startPlain = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            SmallObject obj = new SmallObject();
            obj.set(i, i);
            checksum += obj.x;
        }
        long plainMs = (System.nanoTime() - startPlain) / 1_000_000;

        SmallObjectPool pool = new SmallObjectPool();
        long startPooled = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            SmallObject obj = pool.acquire();
            obj.set(i, i);
            checksum += obj.x;
            pool.release(obj);
        }
        long pooledMs = (System.nanoTime() - startPooled) / 1_000_000;

        System.out.println(ITERATIONS + " iterations, plain allocation: " + plainMs + "ms on this run");
        System.out.println(ITERATIONS + " iterations, synchronized pool acquire/release: " + pooledMs + "ms on this run");
        System.out.println("(checksum " + checksum + ", used only to prevent the JIT discarding the work)");

        System.out.println();
        System.out.println(pooledMs > plainMs
            ? "CONFIRMED FAILURE: pooling this object is slower than just allocating it fresh every time. "
              + "SmallObject was never expensive enough for reuse to pay for itself -- the synchronization "
              + "the pool needs to be safely shared costs more than the near-free young-gen allocation it "
              + "was built to avoid. The same technique that helped the large buffer actively hurts here."
            : "Unexpected -- investigate.");
    }
}
