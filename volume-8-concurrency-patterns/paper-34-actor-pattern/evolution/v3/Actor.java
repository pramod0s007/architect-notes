import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Note: the worker thread is started by start(), called AFTER construction —
// never from inside the constructor, to avoid publishing an incompletely
// constructed subclass instance to another thread.
abstract class Actor {
    private final BlockingQueue<Runnable> mailbox = new LinkedBlockingQueue<>();

    void start() {
        Thread worker = new Thread(this::processLoop);
        worker.setDaemon(true);
        worker.start();
    }

    protected void send(Runnable message) {
        mailbox.add(message);
    }

    private void processLoop() {
        while (true) {
            try {
                mailbox.take().run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
