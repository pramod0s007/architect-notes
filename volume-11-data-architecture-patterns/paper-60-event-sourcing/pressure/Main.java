public class Main {
    public static void main(String[] args) {
        BalanceOnlyLedger ledger = new BalanceOnlyLedger();
        long[] amounts = {10_000, -2_000, 60_000, -1_500, 5_000};
        for (long amount : amounts) {
            ledger.transact(amount);
        }

        System.out.println("Total balance: " + ledger.balance() + " cents");
        System.out.println("Product now wants a report: \"list every transaction over 5,000 cents.\"");
        System.out.println("Answer: NOT RECOVERABLE. BalanceOnlyLedger never stored the individual");
        System.out.println("transactions -- only their running sum survived. The 60,000-cent deposit");
        System.out.println("above is completely indistinguishable from five separate 12,000-cent ones.");
    }
}
