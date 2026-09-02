import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int STARTING_BALANCE_CENTS = 10_000;

    // Naive consumer: applies every delivery it receives, with no memory of
    // what it already processed. At-least-once delivery means the stream WILL
    // redeliver a message if the consumer crashes (or is slow to ack) after
    // processing but before committing its offset -- this consumer has no way
    // to tell that redelivery apart from a brand new event.
    static int balanceCents = STARTING_BALANCE_CENTS;

    static void process(Event e) {
        balanceCents -= e.amountCents;
        System.out.println("  processed " + e.messageId + ": deducted " + e.amountCents
            + "c, balance now " + balanceCents + "c");
    }

    public static void main(String[] args) {
        // The consumer processes M1 (deducts 1000c), then crashes before
        // committing its offset. The stream, having never seen a commit,
        // redelivers M1 -- the exact same messageId, same content.
        List<Event> deliveries = new ArrayList<>();
        deliveries.add(new Event("M1", "acct-A", 1000));
        deliveries.add(new Event("M1", "acct-A", 1000)); // redelivery after crash, same messageId

        System.out.println("Starting balance: " + STARTING_BALANCE_CENTS + "c");
        System.out.println("One logical charge of 1000c is sent; consumer crashes before committing, causing redelivery:");
        for (Event e : deliveries) {
            process(e);
        }

        int expectedBalance = STARTING_BALANCE_CENTS - 1000; // exactly one logical charge
        System.out.println();
        System.out.println("Expected balance (1 logical charge): " + expectedBalance + "c");
        System.out.println("Actual balance: " + balanceCents + "c");
        System.out.println(balanceCents != expectedBalance
            ? "CONFIRMED FAILURE: the account was charged twice for one logical payment, because "
              + "the consumer has no memory of having already processed messageId M1 and reapplies "
              + "every delivery unconditionally."
            : "Unexpected -- investigate.");
    }
}
