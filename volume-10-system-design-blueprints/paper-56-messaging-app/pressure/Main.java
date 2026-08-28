public class Main {
    public static void main(String[] args) {
        NaiveMessageInbox inbox = new NaiveMessageInbox();
        // server assigned sequence 1, 2, 3 -- but the network delivers 2, 1, 3
        inbox.receive(new Message(2, "how about 7pm?"));
        inbox.receive(new Message(1, "want to grab dinner?"));
        inbox.receive(new Message(3, "sounds good"));

        System.out.println("Sent in order: 1, 2, 3");
        System.out.print("Displayed in order: ");
        for (Message m : inbox.messages()) {
            System.out.print(m.sequenceNumber + " ");
        }
        System.out.println("(WRONG -- reads as a non-sequitur reply thread)");

        NaiveReceiptTracker receipt = new NaiveReceiptTracker();
        receipt.update(ReceiptStatus.DELIVERED);
        receipt.update(ReceiptStatus.READ);      // user opened the chat and read it
        receipt.update(ReceiptStatus.DELIVERED); // a delayed DELIVERED receipt arrives late

        System.out.println("Receipt sequence: DELIVERED -> READ -> DELIVERED (late arrival)");
        System.out.println("Final status shown to sender: " + receipt.status() + " (WRONG -- was already Read)");
    }
}
