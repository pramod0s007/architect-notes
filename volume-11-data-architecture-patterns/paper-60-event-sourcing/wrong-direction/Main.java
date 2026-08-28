import java.util.List;

public class Main {
    public static void main(String[] args) {
        BalanceSnapshotLedger ledger = new BalanceSnapshotLedger();
        ledger.transact(TransactionType.DEPOSIT, 10_000);
        ledger.transact(TransactionType.WITHDRAWAL, -2_000);
        ledger.transact(TransactionType.FEE, -500);
        ledger.transact(TransactionType.DEPOSIT, 60_000);
        ledger.transact(TransactionType.WITHDRAWAL, -1_500);

        System.out.println("Total balance: " + ledger.balance() + " cents");

        List<Long> snapshots = ledger.snapshots();
        System.out.print("Balance snapshots stored: ");
        for (long s : snapshots) System.out.print(s + " ");
        System.out.println();

        System.out.println("Product wants a report: \"total FEE amount collected.\"");
        System.out.println("Reconstructing deltas by diffing snapshots: +10000, -2000, -500, +60000, -1500");
        System.out.println("Answer: NOT RECOVERABLE. The -2000 withdrawal and the -500 fee are both just");
        System.out.println("negative numbers now -- the TransactionType passed into every transact() call");
        System.out.println("was accepted and immediately thrown away. The balance is fine. The business");
        System.out.println("question about WHY it changed can never be answered from what was stored.");
    }
}
