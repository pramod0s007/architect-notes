import java.util.ArrayList;
import java.util.List;

/**
 * A write-ahead log where every append is immediately treated as
 * durable -- simulating a synchronous fsync on every single log write,
 * before the caller is ever told the write succeeded. There is no
 * buffering window where an entry exists only in volatile memory.
 */
public class WriteAheadLog {
    private final List<String> durable = new ArrayList<>();

    /** Appends the entry and only returns once it's durable (fsynced). */
    public void appendAndFsync(String entry) {
        durable.add(entry); // this line stands in for a real fsync() call
    }

    public List<String> durableEntries() {
        return durable;
    }
}
