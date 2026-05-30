// Paper 01 — Example 3: Behavior variation → Command Pattern

enum TransactionType {
    DEPOSIT,
    WITHDRAW
}

class BankAccount {

    private double balance;

    void execute(TransactionType type, double amount) {
        if (type == TransactionType.DEPOSIT) {
            balance += amount;
            return;
        }

        if (type == TransactionType.WITHDRAW) {
            if (amount > balance) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            balance -= amount;
        }
    }

    void undo(TransactionType type, double amount) {
        if (type == TransactionType.DEPOSIT) {
            balance -= amount;
            return;
        }

        if (type == TransactionType.WITHDRAW) {
            balance += amount;
        }
    }

    double getBalance() {
        return balance;
    }
}
