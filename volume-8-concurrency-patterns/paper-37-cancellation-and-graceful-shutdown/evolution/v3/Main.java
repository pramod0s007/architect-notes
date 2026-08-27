import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        runCooperativeWorkerDemo();
        runInterruptHidingDemo();
    }

    private static void runCooperativeWorkerDemo() throws InterruptedException {
        BlockingQueue<Job> queue = new LinkedBlockingQueue<>();
        CooperativeWorker worker = new CooperativeWorker(queue);
        Thread thread = new Thread(worker);
        thread.start();

        thread.interrupt(); // simulates shutdownNow()
        thread.join(2000);

        System.out.println("Thread stopped: " + !thread.isAlive());
        System.out.println("v3: interrupted, and actually stopped within one take() call.");
    }

    private static void runInterruptHidingDemo() throws InterruptedException {
        boolean callerSawInterrupt = InterruptHidingDemo.runAndCheckIfCallerSeesInterrupt();
        System.out.println("Caller saw the interrupt after inner swallow: " + callerSawInterrupt);
        System.out.println("v3: swallowing without restoring hides the interrupt from the caller.");
        Thread.interrupted(); // clear status so it doesn't leak into JVM shutdown
    }
}
