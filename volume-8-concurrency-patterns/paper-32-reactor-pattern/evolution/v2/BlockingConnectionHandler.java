import java.io.IOException;
import java.io.InputStream;

class BlockingConnectionHandler implements Runnable {
    private final InputStream connection;

    BlockingConnectionHandler(InputStream connection) {
        this.connection = connection;
    }

    public void run() {
        try {
            int data;
            while ((data = connection.read()) != -1) {
                process(data);
            }
        } catch (IOException e) {
            // connection closed
        }
    }

    private void process(int data) {
        // no-op: these simulated connections never actually send anything
    }
}
