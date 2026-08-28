public class Main {
    public static void main(String[] args) throws InterruptedException {
        AsyncReadLedger readLedger = new AsyncReadLedger();
        WriteLedger writeLedger = new WriteLedger(readLedger);

        writeLedger.transact(10_000); // a $100.00 deposit

        System.out.println("Write model balance right after the deposit: " + writeLedger.balance() + " cents (correct, immediately)");
        System.out.println("Read model balance right after the SAME deposit: " + readLedger.balance() + " cents (WRONG -- the customer who just deposited can't see it)");

        Thread.sleep(250); // wait for the 200ms replication lag to actually land

        System.out.println("Read model balance 250ms later: " + readLedger.balance() + " cents (correct, once replication catches up)");
    }
}
