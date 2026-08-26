import java.util.concurrent.TimeUnit;

// Fix 2: tryLock with a timeout, and randomized backoff before retrying.
// No lock is ever held forever, so "hold and wait" can't persist into a permanent cycle —
// and the randomized delay prevents both threads from retrying in lockstep (livelock).
class TransferServiceWithTimeout {
    void transfer(Account from, Account to, double amount) throws InterruptedException {
        while (true) {
            if (from.getLock().tryLock()) {
                try {
                    if (to.getLock().tryLock(50, TimeUnit.MILLISECONDS)) {
                        try {
                            from.debit(amount);
                            to.credit(amount);
                            return;
                        } finally {
                            to.getLock().unlock();
                        }
                    }
                } finally {
                    from.getLock().unlock();
                }
            }
            Thread.sleep((long) (Math.random() * 10));
        }
    }
}
