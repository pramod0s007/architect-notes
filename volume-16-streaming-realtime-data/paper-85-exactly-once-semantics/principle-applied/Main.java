import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    static final int STARTING_BALANCE_CENTS = 10_000;
    static int balanceCents = STARTING_BALANCE_CENTS;

    // Idempotent consumer: tracks every messageId it has already applied.
    // A redelivery carries the SAME messageId as the original, so it's
    // recognized and skipped -- but a genuinely new event, even with
    // identical accountId/amount, carries a DIFFERENT messageId and is
    // still correctly applied.
    static final Set<String> processedMessageIds = new HashSet<>();

    static void process(Event e) {
        if (processedMessageIds.contains(e.messageId)) {
            System.out.println("  skipped " + e.messageId + " (already processed) -- balance unchanged at " + balanceCents + "c");
            return;
        }
        balanceCents -= e.amountCents;
        processedMessageIds.add(e.messageId);
        System.out.println("  processed " + e.messageId + ": deducted " + e.amountCents
            + "c, balance now " + balanceCents + "c");
    }

    public static void main(String[] args) {
        // Same redelivery scenario as the pressure case, PLUS a genuinely new,
        // distinct second charge for the identical account and amount (e.g.
        // the customer buys a second identical $10 item minutes later).
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
        System.out.println(balanceCents == expectedBalance
            ? "FIXED: the M1 redelivery was recognized and skipped by messageId, while the genuinely "
              + "distinct M2 charge -- same account, same amount, different messageId -- was still "
              + "correctly applied. Deduplication by messageId tells redelivery apart from a real "
              + "second event without needing to inspect the event's content at all."
            : "Unexpected -- investigate.");
    }
}
