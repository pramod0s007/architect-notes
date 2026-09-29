import java.util.ArrayList;
import java.util.List;

/**
 * A write-ahead log that buffers log entries in memory and only
 * considers them durable once a batch of entries is explicitly
 * flushed (simulating an fsync of a buffered write). Most calls to
 * append() only add the entry to the in-memory buffer -- the entry
 * only becomes durable once the buffer happens to reach the flush
 * threshold, at which point that same call also triggers the flush.
 * Either way, the caller is never told which case just happened; it
 * always gets back the same immediate acknowledgment.
 */
public class WriteAheadLog {
    private static final int FLUSH_THRESHOLD = 10;

    private final List<String> buffer = new ArrayList<>();
    private final List<String> durable = new ArrayList<>();

    /** Buffers the entry. Returns immediately -- the caller is not told whether this is durable yet. */
    public void append(String entry) {
        buffer.add(entry);
        if (buffer.size() >= FLUSH_THRESHOLD) {
            flush();
        }
    }

    private void flush() {
        durable.addAll(buffer);
        buffer.clear();
    }

    /** Everything actually durable right now -- i.e. what would survive a crash this instant. */
    public List<String> durableEntries() {
        return durable;
    }

    public int bufferedCount() {
        return buffer.size();
    }
}
