public class Main {
    public static void main(String[] args) throws InterruptedException {
        Account account = new Account(1, 100.0);
        AccountService service = new AccountService();

        Thread t1 = new Thread(() -> service.deposit(account, 10.0));
        Thread t2 = new Thread(() -> service.deposit(account, 20.0));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Final balance: " + account.getBalance());
        System.out.println("v1: one lock, many threads, zero risk of deadlock.");
    }
}
