// Fix 1: order every lock acquisition by a stable global key (account id).
// Every thread wants the lower-id account's lock first — no cycle can form.
class TransferServiceOrdered {
    void transfer(Account from, Account to, double amount) {
        Account first  = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;

        synchronized (first) {
            sleepBriefly();
            synchronized (second) {
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
