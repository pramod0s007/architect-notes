import java.util.ArrayList;
import java.util.List;

class Outbox {
    private final List<OutboxEntry> entries = new ArrayList<>();

    void add(OutboxEntry entry) {
        entries.add(entry);
    }

    List<OutboxEntry> unpublished() {
        List<OutboxEntry> result = new ArrayList<>();
        for (OutboxEntry e : entries) {
            if (!e.published) {
                result.add(e);
            }
        }
        return result;
    }

    List<OutboxEntry> all() {
        return entries;
    }
}
