import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Each shard has its OWN lock and its OWN map. An account's events always
// land on the same shard (a hash of its ID decides which), but two accounts
// on DIFFERENT shards never contend for the same lock at all.
class ShardedEventStore {
    private final Object[] locks;
    private final Map<String, List<Long>>[] shards;

    @SuppressWarnings("unchecked")
    ShardedEventStore(int shardCount) {
        locks = new Object[shardCount];
        shards = new HashMap[shardCount];
        for (int i = 0; i < shardCount; i++) {
            locks[i] = new Object();
            shards[i] = new HashMap<>();
        }
    }

    void append(String accountId, long amountCents) {
        int shard = shardFor(accountId);
        synchronized (locks[shard]) {
            shards[shard].computeIfAbsent(accountId, k -> new ArrayList<>()).add(amountCents);
            simulateWork();
        }
    }

    int shardFor(String accountId) {
        return Math.floorMod(accountId.hashCode(), locks.length);
    }

    private void simulateWork() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
