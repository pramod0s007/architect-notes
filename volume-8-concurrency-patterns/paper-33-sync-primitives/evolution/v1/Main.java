public class Main {
    public static void main(String[] args) {
        SingleThreadedCounter counter = new SingleThreadedCounter();
        for (int i = 0; i < 50_000; i++) {
            counter.increment();
        }
        System.out.println("Expected 50000, got " + counter.get());
        System.out.println("v1: single thread, no race possible, no primitive needed.");
    }
}
