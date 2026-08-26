import java.io.IOException;
import java.io.InputStream;

class BlockingConnectionHandler implements Runnable {
    private final InputStream connection;
    private final String name;

    BlockingConnectionHandler(InputStream connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    public void run() {
        try {
            int data;
            while ((data = connection.read()) != -1) {
                System.out.println(name + " received byte: " + data);
            }
        } catch (IOException e) {
            // connection closed
        }
    }
}
