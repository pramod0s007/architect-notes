/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) throws InterruptedException {
        StopWatch watch = new StopWatch();

        System.out.println("Initial: " + watch.stateName());

        watch.start();
        System.out.println("start  -> " + watch.stateName());
        Thread.sleep(50);

        watch.pause();
        System.out.println("pause  -> " + watch.stateName() + ", elapsed=" + watch.elapsedMillis() + "ms");

        watch.resume();
        System.out.println("resume -> " + watch.stateName());
        Thread.sleep(30);

        watch.stop();
        System.out.println("stop   -> " + watch.stateName() + ", elapsed=" + watch.elapsedMillis() + "ms");
    }
}
