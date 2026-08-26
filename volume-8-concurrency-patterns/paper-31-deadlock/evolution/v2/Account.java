class Account {
    private final int id;
    private double balance;

    Account(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    int getId() {
        return id;
    }

    double getBalance() {
        return balance;
    }

    void debit(double amount) {
        balance -= amount;
    }

    void credit(double amount) {
        balance += amount;
    }
}
