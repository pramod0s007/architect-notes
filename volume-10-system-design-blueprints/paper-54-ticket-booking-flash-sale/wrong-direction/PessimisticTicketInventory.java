// Locks the whole check-then-act sequence. Correct by construction -- no two
// threads can ever both pass the check before either one decrements -- at
// the cost of serializing every booking attempt through one lock.
class PessimisticTicketInventory {
    private int seatsAvailable;

    PessimisticTicketInventory(int seats) {
        this.seatsAvailable = seats;
    }

    synchronized boolean bookSeat() {
        if (seatsAvailable > 0) {
            seatsAvailable--;
            return true;
        }
        return false;
    }

    synchronized int seatsAvailable() {
        return seatsAvailable;
    }
}
