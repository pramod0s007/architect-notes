import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        // A handful of connections — one thread each is simple and perfectly fine here.
        for (int i = 1; i <= 3; i++) {
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(out);
            Thread handlerThread = new Thread(new BlockingConnectionHandler(in, "connection-" + i));
            handlerThread.start();

            out.write(65 + i); // send one byte
            out.close();
            handlerThread.join();
        }
        System.out.println("v1: 3 connections, 3 threads. Simple, debuggable, fine at this scale.");
    }
}
