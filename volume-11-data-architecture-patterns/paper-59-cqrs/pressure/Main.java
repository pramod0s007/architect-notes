public class Main {
    public static void main(String[] args) throws InterruptedException {
        Ledger ledger = new Ledger();
        for (int i = 0; i < 50; i++) {
            ledger.transact(100); // 50 prior transactions, so statement() has real work to do
        }

        // Baseline: how long does a transaction take with no read contention?
        long baselineStart = System.nanoTime();
        ledger.transact(500);
        long baselineMs = (System.nanoTime() - baselineStart) / 1_000_000;

        // Now start a thread continuously generating statements (a realistic
        // customer-facing read pattern) and measure a transaction submitted
        // WHILE that read is in progress.
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                ledger.statement();
            }
        });
        reader.start();
        Thread.sleep(10); // let the reader acquire the lock first

        long contendedStart = System.nanoTime();
        ledger.transact(500);
        long contendedMs = (System.nanoTime() - contendedStart) / 1_000_000;

        reader.join();

        System.out.println("Transaction latency with no concurrent reads: " + baselineMs + "ms");
        System.out.println("Transaction latency while a statement is being generated: " + contendedMs + "ms");
        System.out.println("The write waited behind the read, because both share the same lock.");
    }
}
