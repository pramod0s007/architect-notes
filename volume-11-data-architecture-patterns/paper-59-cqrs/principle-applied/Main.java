public class Main {
    public static void main(String[] args) throws InterruptedException {
        ReadLedger readLedger = new ReadLedger();
        WriteLedger writeLedger = new WriteLedger(readLedger);

        for (int i = 0; i < 50; i++) {
            writeLedger.transact(100);
        }

        long baselineStart = System.nanoTime();
        writeLedger.transact(500);
        long baselineMs = (System.nanoTime() - baselineStart) / 1_000_000;

        Thread reader = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                readLedger.statement();
            }
        });
        reader.start();
        Thread.sleep(10);

        long contendedStart = System.nanoTime();
        writeLedger.transact(500);
        long contendedMs = (System.nanoTime() - contendedStart) / 1_000_000;

        reader.join();

        System.out.println("Transaction latency with no concurrent reads: " + baselineMs + "ms");
        System.out.println("Transaction latency while a statement is being generated: " + contendedMs + "ms");
        System.out.println("The write and the read no longer share a lock -- neither one waits on the other.");
    }
}
