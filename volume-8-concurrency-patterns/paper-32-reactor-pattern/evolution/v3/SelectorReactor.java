import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

// One thread runs this entire loop, no matter how many channels are registered.
class SelectorReactor {
    private final Selector selector;
    private int openChannels = 0;

    SelectorReactor() throws IOException {
        this.selector = Selector.open();
    }

    void register(Pipe.SourceChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        openChannels++;
    }

    // Runs until every registered channel has been closed. Exactly one thread, ever.
    //
    // Uses a timed select() rather than an indefinite one: on macOS, an indefinite
    // select() was observed to not reliably deliver the EOF-readiness event for a
    // closed Pipe.SourceChannel, leaving the reactor blocked forever after correctly
    // reading all data. A timed select() reliably surfaces EOF on the next pass.
    //
    // Termination is tracked with an explicit openChannels counter rather than
    // selector.keys().isEmpty() — a cancelled key stays in keys() until the *next*
    // select() call, so gating on keys() forces one extra, otherwise-pointless
    // select(1000) wait after the last channel closes.
    void run() throws IOException {
        while (openChannels > 0) {
            selector.select(1000);

            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (key.isReadable()) {
                    Pipe.SourceChannel channel = (Pipe.SourceChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    int bytesRead = channel.read(buffer);

                    if (bytesRead > 0) {
                        buffer.flip();
                        StringBuilder sb = new StringBuilder();
                        while (buffer.hasRemaining()) sb.append((char) buffer.get());
                        System.out.println("Reactor thread received: " + sb);
                    } else if (bytesRead == -1) {
                        key.cancel();
                        channel.close();
                        openChannels--;
                    }
                }
            }
        }
    }
}
