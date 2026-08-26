// Locks acquired in ARGUMENT order — from first, to second. Two opposite-direction
// transfers acquire the same two locks in opposite order: circular wait waiting to happen.
class TransferService {
    void transfer(Account from, Account to, double amount) {
        synchronized (from) {
            sleepBriefly(); // widens the race window so the deadlock reproduces reliably
            synchronized (to) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
