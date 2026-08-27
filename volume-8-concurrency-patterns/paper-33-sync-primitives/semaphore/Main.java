public class Main {
    public static void main(String[] args) throws InterruptedException {
        int maxConcurrent = 3;
        ConnectionPool pool = new ConnectionPool(maxConcurrent);
        int callers = 20;

        Thread[] threads = new Thread[callers];
        for (int i = 0; i < callers; i++) {
            threads[i] = new Thread(() -> {
                try {
                    pool.useConnection(() -> {
                        try {
                            Thread.sleep(20); // simulate holding the connection briefly
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("20 callers, pool size " + maxConcurrent
                + ", max observed concurrency: " + pool.getMaxObservedConcurrency());
        System.out.println("A lock would have allowed only 1 at a time. A Semaphore(3) allows up to 3.");
    }
}
