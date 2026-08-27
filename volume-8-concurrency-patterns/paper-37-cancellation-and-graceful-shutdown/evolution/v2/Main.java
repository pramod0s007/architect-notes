import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Job> queue = new LinkedBlockingQueue<>();
        Worker worker = new Worker(queue);
        Thread thread = new Thread(worker);
        thread.setDaemon(true); // demo only: this worker genuinely cannot be cancelled
        thread.start();

        thread.interrupt(); // simulates shutdownNow()
        Thread.sleep(100);  // give the interrupt time to be delivered

        // Feed it more jobs AFTER interrupting — a truly-cancelled worker would never see these.
        for (int i = 0; i < 5; i++) {
            queue.put(new Job(i));
        }
        Thread.sleep(100);

        System.out.println("Thread still alive after interrupt: " + thread.isAlive());
        System.out.println("Interrupt was seen: " + worker.interruptSeen);
        System.out.println("Jobs processed AFTER the interrupt: " + worker.processedAfterInterrupt.get());
        System.out.println("v2: interrupted, but kept consuming jobs. This thread cannot be cancelled.");
    }
}
