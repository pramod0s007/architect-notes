import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) throws Exception {
        // An actor's mailbox + single worker thread IS a single-threaded executor.
        // This demonstrates the anti-pattern directly: blocking that one thread
        // waiting, unconditionally, for a task that same thread must itself run.
        ExecutorService singleThreaded = Executors.newSingleThreadExecutor();

        Future<?> outer = singleThreaded.submit(() -> {
            System.out.println("outer task running on the only worker thread");
            Future<?> inner = singleThreaded.submit(() ->
                    System.out.println("inner task (should never print — outer holds the only thread)"));
            try {
                inner.get(); // unconditional block — genuinely permanent: inner is queued
                             // behind THIS very thread, which is now blocked waiting for it
            } catch (InterruptedException e) {
                System.out.println("outer's wait was interrupted (expected during forced shutdown)");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.out.println("outer's wait failed: " + e);
            }
        });

        try {
            outer.get(3, TimeUnit.SECONDS); // the TEST HARNESS bounds the wait — the deadlock itself has no timeout
            System.out.println("outer completed (should never happen)");
        } catch (TimeoutException e) {
            System.out.println("Self-deadlock confirmed: outer is still blocked after 3s.");
            System.out.println("inner can never run — the only thread that could run it is the one waiting for it.");
            singleThreaded.shutdownNow(); // forced recovery, same as killing a hung process in production
        }
    }
}
