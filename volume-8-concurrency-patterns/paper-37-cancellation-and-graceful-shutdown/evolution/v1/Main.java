public class Main {
    public static void main(String[] args) throws InterruptedException {
        SimpleWorker worker = new SimpleWorker();
        Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(50); // let it spin for a bit
        worker.requestStop();
        thread.join(2000);

        System.out.println("Thread stopped: " + !thread.isAlive());
        System.out.println("Iterations completed: " + worker.iterations.get());
        System.out.println("v1: a non-blocking loop with a checked flag stops promptly.");
    }
}
