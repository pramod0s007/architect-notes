class AccountActor extends Actor {
    private double balance;

    AccountActor(double balance) {
        this.balance = balance;
    }

    void debit(double amount, Runnable onDone) {
        send(() -> { balance -= amount; onDone.run(); });
    }

    void credit(double amount, Runnable onDone) {
        send(() -> { balance += amount; onDone.run(); });
    }

    void getBalance(java.util.function.Consumer<Double> onResult) {
        send(() -> onResult.accept(balance));
    }
}
