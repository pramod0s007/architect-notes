import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        int channelCount = 20;
        SelectorReactor reactor = new SelectorReactor();
        Pipe[] pipes = new Pipe[channelCount];

        for (int i = 0; i < channelCount; i++) {
            Pipe pipe = Pipe.open();
            pipes[i] = pipe;
            reactor.register(pipe.source());
        }

        // Separate writer threads simulate external data arriving on each channel —
        // they are NOT "one thread per connection"; they just inject data, then exit.
        // The reactor thread below is the ONLY thread that ever reads or processes.
        for (int i = 0; i < channelCount; i++) {
            final int idx = i;
            final Pipe pipe = pipes[i];
            new Thread(() -> {
                try {
                    pipe.sink().write(java.nio.ByteBuffer.wrap(
                            ("msg-from-channel-" + idx).getBytes(StandardCharsets.UTF_8)));
                    pipe.sink().close();
                } catch (Exception ignored) {
                }
            }).start();
        }

        long startNanos = System.nanoTime();
        reactor.run(); // exactly ONE thread (this one) services all 20 channels
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        System.out.println("v3: " + channelCount + " channels serviced by 1 reactor thread in " + elapsedMs + "ms");
    }
}
