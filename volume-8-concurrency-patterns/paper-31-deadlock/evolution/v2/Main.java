public class Main {
    public static void main(String[] args) throws InterruptedException {
        Account accountA = new Account(1, 100.0);
        Account accountB = new Account(2, 100.0);
        TransferService transferService = new TransferService();

        Thread t1 = new Thread(() -> transferService.transfer(accountA, accountB, 50.0));
        Thread t2 = new Thread(() -> transferService.transfer(accountB, accountA, 30.0));
        t1.start();
        t2.start();

        t1.join(2000);
        t2.join(2000);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("DEADLOCK DETECTED: both threads still blocked after 2s");
            System.exit(1);
        }
        System.out.println("v2: completed without deadlock this run (not guaranteed).");
    }
}
