public class Main {
    static final int THREADS = 20;
    static final int REQUESTS_PER_THREAD = 2500;

    public static void main(String[] args) throws InterruptedException {
        RequestCounter plain = new RequestCounter();
        runStartAndFinish(plain::requestStarted, plain::requestFinished);
        System.out.println("Plain int:      expected 0, got " + plain.getInFlight());

        RequestCounterVolatile vol = new RequestCounterVolatile();
        runStartAndFinish(vol::requestStarted, vol::requestFinished);
        System.out.println("volatile int:   expected 0, got " + vol.getInFlight());
        System.out.println("v2: volatile did not fix the lost updates. Same bug, same shape.");
    }

    private static void runStartAndFinish(Runnable start, Runnable finish) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    start.run();
                    finish.run();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}
