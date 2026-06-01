public final class BankAccount {

    private final String id;
    private double balance;

    public BankAccount(String id, double initialBalance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("initialBalance must not be negative");
        }
        this.id = id;
        this.balance = initialBalance;
    }

    public String id() {
        return id;
    }

    public double balance() {
        return balance;
    }

    void deposit(double amount) {
        requirePositive(amount);
        balance += amount;
    }

    void withdraw(double amount) {
        requirePositive(amount);
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds on account " + id);
        }
        balance -= amount;
    }

    private static void requirePositive(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
