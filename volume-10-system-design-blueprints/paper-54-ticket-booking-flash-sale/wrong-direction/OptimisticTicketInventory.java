import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// No lock at all. Reads the current count, computes the new count, and
// commits only if nobody else changed it in between -- if someone did,
// retry from scratch. Correct by construction, and nothing ever blocks.
class OptimisticTicketInventory {
    private final AtomicInteger seatsAvailable;
    private final AtomicLong retries = new AtomicLong(0);

    OptimisticTicketInventory(int seats) {
        this.seatsAvailable = new AtomicInteger(seats);
    }

    boolean bookSeat() {
        while (true) {
            int current = seatsAvailable.get();
            if (current <= 0) {
                return false;
            }
            if (seatsAvailable.compareAndSet(current, current - 1)) {
                return true;
            }
            retries.incrementAndGet(); // someone else updated first -- retry
        }
    }

    int seatsAvailable() {
        return seatsAvailable.get();
    }

    long retryCount() {
        return retries.get();
    }
}
