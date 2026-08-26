import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        runOrderedFix();
        runTimeoutFix();
        runNaiveRetryComparison();
    }

    private static void runOrderedFix() throws InterruptedException {
        Account accountA = new Account(1, 100.0);
        Account accountB = new Account(2, 100.0);
        TransferServiceOrdered service = new TransferServiceOrdered();

        Thread t1 = new Thread(() -> service.transfer(accountA, accountB, 50.0));
        Thread t2 = new Thread(() -> service.transfer(accountB, accountA, 30.0));
        t1.start();
        t2.start();
        t1.join(2000);
        t2.join(2000);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("Ordered fix: DEADLOCK (should never happen)");
        } else {
            System.out.println("Ordered fix: completed. A=" + accountA.getBalance() + " B=" + accountB.getBalance());
        }
    }

    private static void runTimeoutFix() throws InterruptedException {
        Account accountA = new Account(1, 100.0);
        Account accountB = new Account(2, 100.0);
        TransferServiceWithTimeout service = new TransferServiceWithTimeout();

        Thread t1 = new Thread(() -> {
            try { service.transfer(accountA, accountB, 50.0); } catch (InterruptedException ignored) {}
        });
        Thread t2 = new Thread(() -> {
            try { service.transfer(accountB, accountA, 30.0); } catch (InterruptedException ignored) {}
        });
        t1.start();
        t2.start();
        t1.join(2000);
        t2.join(2000);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("Timeout fix: DEADLOCK (should never happen)");
        } else {
            System.out.println("Timeout fix: completed. A=" + accountA.getBalance() + " B=" + accountB.getBalance());
        }
    }

    // 8 threads contending on the same 2 accounts — naive retry (no backoff) vs. tryLock+backoff.
    private static void runNaiveRetryComparison() throws InterruptedException {
        Account accountA = new Account(1, 1000.0);
        Account accountB = new Account(2, 1000.0);
        TransferServiceNaiveRetry naive = new TransferServiceNaiveRetry();

        List<Thread> naiveThreads = new ArrayList<>();
        long naiveStart = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            boolean forward = i % 2 == 0;
            Thread t = new Thread(() -> {
                if (forward) naive.transfer(accountA, accountB, 1.0);
                else naive.transfer(accountB, accountA, 1.0);
            });
            naiveThreads.add(t);
            t.start();
        }
        for (Thread t : naiveThreads) t.join(15000);
        long naiveElapsedMs = (System.nanoTime() - naiveStart) / 1_000_000;
        System.out.println("Naive retry (no backoff), 8 threads: " + naive.attempts.get()
                + " attempts, " + naiveElapsedMs + "ms");

        Account accountC = new Account(3, 1000.0);
        Account accountD = new Account(4, 1000.0);
        TransferServiceWithTimeout backoff = new TransferServiceWithTimeout();

        List<Thread> backoffThreads = new ArrayList<>();
        long backoffStart = System.nanoTime();
        for (int i = 0; i < 8; i++) {
            boolean forward = i % 2 == 0;
            Thread t = new Thread(() -> {
                try {
                    if (forward) backoff.transfer(accountC, accountD, 1.0);
                    else backoff.transfer(accountD, accountC, 1.0);
                } catch (InterruptedException ignored) {
                }
            });
            backoffThreads.add(t);
            t.start();
        }
        for (Thread t : backoffThreads) t.join(15000);
        long backoffElapsedMs = (System.nanoTime() - backoffStart) / 1_000_000;
        System.out.println("Timeout+backoff, 8 threads: " + backoffElapsedMs + "ms (no attempt counter needed)");
    }
}
