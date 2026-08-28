// Check-then-act with no protection at all. A 1ms sleep between the check
// and the decrement widens the race window so the bug reproduces reliably
// in a short-running demo -- the SAME race exists without it, just harder
// to catch in a few thousand iterations.
class UnsynchronizedTicketInventory {
    private int seatsAvailable;

    UnsynchronizedTicketInventory(int seats) {
        this.seatsAvailable = seats;
    }

    boolean bookSeat() {
        if (seatsAvailable > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            seatsAvailable--;
            return true;
        }
        return false;
    }

    int seatsAvailable() {
        return seatsAvailable;
    }
}
