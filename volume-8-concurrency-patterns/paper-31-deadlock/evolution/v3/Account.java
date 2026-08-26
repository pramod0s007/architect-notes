import java.util.concurrent.locks.ReentrantLock;

class Account {
    private final int id;
    private double balance;
    private final ReentrantLock lock = new ReentrantLock();

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

    ReentrantLock getLock() {
        return lock;
    }
}
