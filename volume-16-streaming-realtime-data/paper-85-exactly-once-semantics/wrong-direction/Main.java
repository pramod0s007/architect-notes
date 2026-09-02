import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    static final int STARTING_BALANCE_CENTS = 10_000;
    static int balanceCents = STARTING_BALANCE_CENTS;

    // Overcorrection: dedup by a hash of the event's CONTENT (accountId +
    // amountCents) instead of its messageId. This still catches a true
    // redelivery -- but it also catches a genuinely distinct event that
    // happens to carry identical content, and silently drops it.
    static final Set<String> seenContentKeys = new HashSet<>();

    static String contentKey(Event e) {
        return e.accountId + ":" + e.amountCents;
    }

    static void process(Event e) {
        String key = contentKey(e);
        if (seenContentKeys.contains(key)) {
            System.out.println("  skipped " + e.messageId + " (content key \"" + key
                + "\" already seen) -- balance unchanged at " + balanceCents + "c");
            return;
        }
        balanceCents -= e.amountCents;
        seenContentKeys.add(key);
        System.out.println("  processed " + e.messageId + ": deducted " + e.amountCents
            + "c, balance now " + balanceCents + "c");
    }

    public static void main(String[] args) {
        // Identical scenario to principle-applied: M1 charged, redelivered
        // after a crash, then a genuinely new M2 charge for the same account
        // and the same amount.
        List<Event> deliveries = new ArrayList<>();
        deliveries.add(new Event("M1", "acct-A", 1000));
        deliveries.add(new Event("M1", "acct-A", 1000)); // redelivery after crash, same messageId
        deliveries.add(new Event("M2", "acct-A", 1000)); // a real, distinct second charge

        System.out.println("Starting balance: " + STARTING_BALANCE_CENTS + "c");
        System.out.println("M1 charged, redelivered after a crash, then a genuinely new M2 charge arrives:");
        for (Event e : deliveries) {
            process(e);
        }

        int expectedBalance = STARTING_BALANCE_CENTS - 1000 - 1000; // two real logical charges
        System.out.println();
        System.out.println("Expected balance (2 real logical charges: M1 + M2): " + expectedBalance + "c");
        System.out.println("Actual balance: " + balanceCents + "c");
        System.out.println(balanceCents != expectedBalance
            ? "CONFIRMED FAILURE: M2 -- a genuinely distinct charge with its own messageId -- was "
              + "silently dropped, because its content (same account, same amount) hashed to the "
              + "same key as M1's already-seen content. Content-based dedup correctly caught the true "
              + "redelivery, then incorrectly caught a real, separate event that just happened to look "
              + "the same. The customer was undercharged by 1000c and there is no error, log line, or "
              + "metric that distinguishes this from the dedup working exactly as designed."
            : "Unexpected -- investigate.");
    }
}
