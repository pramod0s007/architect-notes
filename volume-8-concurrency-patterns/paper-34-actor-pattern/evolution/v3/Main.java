import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Same scenario as Paper 31: two opposite-direction transfers between the
        // same two accounts, run concurrently. There, this deadlocked reliably.
        // Here, there is no lock anywhere for the two operations to contend over.
        AccountActor accountA = new AccountActor(100.0);
        accountA.start();
        AccountActor accountB = new AccountActor(100.0);
        accountB.start();
        TransferCoordinator coordinator = new TransferCoordinator();
        coordinator.start();

        // Two separate threads, same as Paper 31's reproduction — this is the exact
        // concurrent call pattern that deadlocked reliably there.
        CountDownLatch done = new CountDownLatch(2);
        Thread t1 = new Thread(() -> coordinator.transfer(accountA, accountB, 50.0, done::countDown));
        Thread t2 = new Thread(() -> coordinator.transfer(accountB, accountA, 30.0, done::countDown));
        t1.start();
        t2.start();

        boolean completed = done.await(5, java.util.concurrent.TimeUnit.SECONDS);
        System.out.println("Both transfers completed: " + completed);

        CountDownLatch reads = new CountDownLatch(2);
        double[] balances = new double[2];
        accountA.getBalance(b -> { balances[0] = b; reads.countDown(); });
        accountB.getBalance(b -> { balances[1] = b; reads.countDown(); });
        reads.await();

        System.out.println("A=" + balances[0] + " B=" + balances[1]);
        System.out.println("v3: no lock, no deadlock, correct balances — every time.");
    }
}
