import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Note: the worker thread is started by start(), called AFTER construction —
// never from inside the constructor. Starting a thread that captures `this` from
// within a constructor publishes an incompletely-constructed object to another
// thread (a subclass's own fields may not be initialized yet).
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
