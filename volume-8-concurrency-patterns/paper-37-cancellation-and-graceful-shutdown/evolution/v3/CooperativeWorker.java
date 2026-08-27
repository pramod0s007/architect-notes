import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class CooperativeWorker implements Runnable {
    private final BlockingQueue<Job> queue;
    final AtomicInteger processedAfterInterrupt = new AtomicInteger(0);
    volatile boolean interruptSeen = false;

    CooperativeWorker(BlockingQueue<Job> queue) {
        this.queue = queue;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Job job = queue.take();
                if (interruptSeen) {
                    processedAfterInterrupt.incrementAndGet();
                }
                process(job);
            } catch (InterruptedException e) {
                interruptSeen = true;
                Thread.currentThread().interrupt(); // restore the flag the catch just cleared
                break; // and actually stop
            }
        }
    }

    private void process(Job job) {
        // no-op: standing in for real work
    }
}
