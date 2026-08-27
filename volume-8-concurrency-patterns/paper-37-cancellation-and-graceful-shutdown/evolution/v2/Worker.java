import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class Worker implements Runnable {
    private final BlockingQueue<Job> queue;
    final AtomicInteger processedAfterInterrupt = new AtomicInteger(0);
    volatile boolean interruptSeen = false;

    Worker(BlockingQueue<Job> queue) {
        this.queue = queue;
    }

    public void run() {
        while (true) { // bug 1: no exit condition ever checked
            try {
                Job job = queue.take();
                if (interruptSeen) {
                    processedAfterInterrupt.incrementAndGet();
                }
                process(job);
            } catch (InterruptedException e) {
                interruptSeen = true; // bug 2: notices, but does nothing to actually stop
            }
        }
    }

    private void process(Job job) {
        // no-op: standing in for real work
    }
}
