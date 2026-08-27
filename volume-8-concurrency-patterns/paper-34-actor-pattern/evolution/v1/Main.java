import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        AccountActor account = new AccountActor(100.0);
        account.start();
        int callers = 1000;
        CountDownLatch done = new CountDownLatch(callers);

        for (int i = 0; i < callers; i++) {
            new Thread(() -> account.credit(1.0, done::countDown)).start();
        }
        done.await();

        CountDownLatch readDone = new CountDownLatch(1);
        double[] finalBalance = new double[1];
        account.getBalance(b -> { finalBalance[0] = b; readDone.countDown(); });
        readDone.await();

        System.out.println("Expected 1100.0, got " + finalBalance[0]);
        System.out.println("v1: 1000 concurrent callers, one actor thread, zero races.");
    }
}
