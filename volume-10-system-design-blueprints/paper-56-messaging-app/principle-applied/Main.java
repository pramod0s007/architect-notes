public class Main {
    public static void main(String[] args) {
        OrderedMessageInbox inbox = new OrderedMessageInbox();
        inbox.receive(new Message(2, "how about 7pm?"));
        inbox.receive(new Message(1, "want to grab dinner?"));
        inbox.receive(new Message(2, "how about 7pm?")); // duplicate delivery, same sequence number
        inbox.receive(new Message(3, "sounds good"));

        System.out.println("Sent in order: 1, 2, 3 (with message 2 delivered twice)");
        System.out.print("Displayed in order: ");
        for (Message m : inbox.messages()) {
            System.out.print(m.sequenceNumber + " ");
        }
        System.out.println("(CORRECT -- reconstructed send order, duplicate dropped)");

        MonotonicReceiptTracker receipt = new MonotonicReceiptTracker();
        receipt.update(ReceiptStatus.DELIVERED);
        receipt.update(ReceiptStatus.READ);
        receipt.update(ReceiptStatus.DELIVERED); // late arrival -- ignored, it's a step backward

        System.out.println("Receipt sequence: DELIVERED -> READ -> DELIVERED (late arrival)");
        System.out.println("Final status shown to sender: " + receipt.status() + " (CORRECT -- never regresses)");
    }
}
