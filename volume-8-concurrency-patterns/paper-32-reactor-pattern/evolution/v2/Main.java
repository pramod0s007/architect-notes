import java.io.PipedInputStream;

public class Main {
    public static void main(String[] args) {
        int connectionCount = 2000;
        long startNanos = System.nanoTime();

        for (int i = 0; i < connectionCount; i++) {
            PipedInputStream in = new PipedInputStream();
            Thread t = new Thread(new BlockingConnectionHandler(in));
            t.setDaemon(true); // demo only: let the JVM exit even though these stay blocked forever
            t.start();
        }

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        System.out.println(connectionCount + " threads started in " + elapsedMs + "ms");
        System.out.println("Approx. stack memory reserved: "
                + (connectionCount * 512L / 1024) + " MB (at 512KB/thread default)");
        System.out.println("v2: every one of these threads is blocked in read(), forever, doing nothing.");
    }
}
