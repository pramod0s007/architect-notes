public class Main {
    public static void main(String[] args) {
        EventSourcedLedger ledger = new EventSourcedLedger();
        long[] amounts = {10_000, -2_000, 60_000, -1_500, 5_000};
        for (long amount : amounts) {
            ledger.transact(amount);
        }

        System.out.println("Total balance: " + ledger.balance() + " cents");

        System.out.println("Product now wants the SAME report, invented after the fact: \"list every transaction over 5,000 cents.\"");
        var large = ledger.largeTransactions(5_000);
        System.out.print("Answer: " + large.size() + " transaction(s) found -- ");
        for (var e : large) {
            System.out.print(e.amountCents + " ");
        }
        System.out.println();
        System.out.println("Nothing about how transactions were recorded had to change to answer a question nobody asked yet.");
    }
}
