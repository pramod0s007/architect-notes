public class Main {
    public static void main(String[] args) {
        TimestampMessageInbox inbox = new TimestampMessageInbox();

        // Alice's phone clock is 5000ms BEHIND Bob's. Bob asks a question,
        // Alice replies a genuine 2000ms later in real time -- but her
        // slow clock stamps her reply as happening BEFORE Bob's question
        // was ever sent.
        long bobClock = 1_000_000L;
        long aliceClock = bobClock - 5000; // Alice's clock is running slow

        TimestampedMessage bobsQuestion = new TimestampedMessage(bobClock, "want to grab dinner?");
        TimestampedMessage alicesReply = new TimestampedMessage(aliceClock + 2000, "sounds good, 7pm?");

        // True send order: Bob's question first, then Alice's reply.
        inbox.receive(bobsQuestion);
        inbox.receive(alicesReply);

        System.out.println("True send order: Bob's question, then Alice's reply.");
        System.out.print("Displayed order (sorted by each device's own clock): ");
        for (TimestampedMessage m : inbox.messages()) {
            System.out.print("\"" + m.text + "\"  ");
        }
        System.out.println();
        System.out.println("Alice's reply shows BEFORE Bob's question -- her clock was behind, not her reply.");

        // Same overcorrection, applied to receipts: reject "older" updates by
        // comparing client-supplied timestamps instead of a monotonic ordinal.
        // The recipient's own device is accurate. The delivery-confirmation
        // relay's clock runs 10 seconds FAST, so its receipt -- which genuinely
        // happened EARLIER in real time -- gets stamped with a LATER number.
        TimestampReceiptTracker receipt = new TimestampReceiptTracker();
        long recipientClock = 998_000L;      // accurate device, reads the message a bit later in real time
        long relayClock = 995_000L + 10_000; // relay's clock is 10s fast; DELIVERED actually happened first

        receipt.update(ReceiptStatus.READ, recipientClock);        // applied: 998,000 > MIN_VALUE
        receipt.update(ReceiptStatus.DELIVERED, relayClock);       // wrongly applied: 1,005,000 > 998,000

        System.out.println("Receipt sequence: READ (t=998,000) then a genuinely-earlier DELIVERED (t=1,005,000, clock 10s fast)");
        System.out.println("Final status shown to sender: " + receipt.status() + " (WRONG -- the timestamp looked newer, the event wasn't)");
    }
}
