public class Main {
    public static void main(String[] args) {
        LocalOrderLedger ledger = new LocalOrderLedger();
        Order order = new Order("ORD-1");

        boolean placed = ledger.placeOrder(order, false); // warehouse out of stock
        System.out.println("v1: one local transaction, one service.");
        System.out.println("    placed=" + placed + ", status=" + ledger.statusOf(order));
        System.out.println("    Failure rolled back cleanly -- there's no OTHER service holding stale state.");
    }
}
