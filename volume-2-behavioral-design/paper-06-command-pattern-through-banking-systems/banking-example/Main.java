/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        BankAccount account = new BankAccount("ACC-001", 100.0);
        Invoker invoker = new Invoker();

        print(account, "open");

        invoker.run(new DepositCommand(account, 50.0));
        print(account, "deposit +50");

        invoker.run(new WithdrawCommand(account, 30.0));
        print(account, "withdraw -30");

        invoker.undo();
        print(account, "undo (reverses withdraw)");

        invoker.undo();
        print(account, "undo (reverses deposit)");
    }

    private static void print(BankAccount account, String step) {
        System.out.printf("[%s] %s balance=%.2f%n", step, account.id(), account.balance());
    }
}
