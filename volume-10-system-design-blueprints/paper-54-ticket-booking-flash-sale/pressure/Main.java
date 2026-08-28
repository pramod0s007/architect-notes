import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int SEATS = 100;
    private static final int REQUESTS = 200;

    public static void main(String[] args) throws InterruptedException {
        UnsynchronizedTicketInventory inventory = new UnsynchronizedTicketInventory(SEATS);
        AtomicInteger successes = new AtomicInteger(0);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(REQUESTS);
        for (int i = 0; i < REQUESTS; i++) {
            Thread t = new Thread(() -> {
                try {
                    startGate.await(); // every thread starts at the same instant
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (inventory.bookSeat()) {
                    successes.incrementAndGet();
                }
                doneGate.countDown();
            });
            t.start();
        }
        startGate.countDown();
        doneGate.await();

        System.out.println("Seats available at start: " + SEATS);
        System.out.println("Concurrent booking requests: " + REQUESTS);
        System.out.println("Successful bookings: " + successes.get());
        System.out.println("Seats remaining (should be 0, may be negative): " + inventory.seatsAvailable());
        System.out.println("Oversold by: " + (successes.get() - SEATS) + " seats");
    }
}
