/**
 * Run: javac *.java && java Main
 *
 * Walks two orders through different lifecycle paths:
 *   1. Happy path: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED -> REFUNDED
 *   2. Early cancel: PENDING -> CANCELLED
 *   3. Demonstrates that illegal transitions throw, not silently ignore.
 */
public final class Main {

    public static void main(String[] args) {

        System.out.println("=== Happy path: full lifecycle ===");
        Order o1 = new Order("ORD-1001", "alice@example.com");
        o1.confirm();
        o1.ship();
        o1.deliver();
        o1.refund();
        System.out.println("Final status: " + o1.getStatus());

        System.out.println();

        System.out.println("=== Early cancel path ===");
        Order o2 = new Order("ORD-1002", "bob@example.com");
        o2.cancel();
        System.out.println("Final status: " + o2.getStatus());

        System.out.println();

        System.out.println("=== Illegal transition guard ===");
        Order o3 = new Order("ORD-1003", "carol@example.com");
        o3.confirm();
        try {
            o3.refund(); // CONFIRMED -> REFUND is not allowed
        } catch (IllegalStateException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
        System.out.println("Status unchanged: " + o3.getStatus());
    }
}
