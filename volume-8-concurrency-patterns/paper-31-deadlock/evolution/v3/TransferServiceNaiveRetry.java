import java.util.concurrent.atomic.AtomicInteger;

// The "fix" that looks right but isn't: tryLock retried with NO backoff.
// Both locks ARE released correctly on failure (unlike a leak) — the anti-pattern
// is retrying at full speed instead of backing off, which wastes enormous amounts
// of CPU respinning through the same failed acquisition before either thread wins.
class TransferServiceNaiveRetry {
    final AtomicInteger attempts = new AtomicInteger(0);

    void transfer(Account from, Account to, double amount) {
        while (true) {
            attempts.incrementAndGet();
            if (from.getLock().tryLock()) {
                try {
                    sleepBriefly(); // widen the contention window, same technique as v2
                    if (to.getLock().tryLock()) {
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
            // no sleep, no backoff — retry immediately at full speed
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
