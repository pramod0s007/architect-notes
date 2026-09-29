import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cache node that registers itself with a shared registry of peer nodes.
 * A write on any node invalidates that key on every registered node
 * (including itself) before the write returns, so the very next read on any
 * node is guaranteed to see a cache miss and go to the backend -- there is
 * no window where a stale value can still be served, unlike a TTL, which
 * only bounds that window without closing it.
 */
public class CacheNode {
    private final String name;
    private final Backend backend;
    private final Map<String, String> cache = new HashMap<>();
    private final List<CacheNode> peers;

    public CacheNode(String name, Backend backend, List<CacheNode> peers) {
        this.name = name;
        this.backend = backend;
        this.peers = peers;
        peers.add(this);
    }

    public String get(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        String value = backend.read(key);
        cache.put(key, value);
        return value;
    }

    /**
     * Writes to the backend, then synchronously broadcasts an invalidation
     * to every node (itself included) before returning. Each node simply
     * drops the key rather than trying to guess the new value, so the next
     * get() on any node is a guaranteed miss that reads the fresh backend
     * value.
     */
    public void write(String key, String value) {
        backend.write(key, value);
        for (CacheNode peer : new ArrayList<>(peers)) {
            peer.invalidate(key);
        }
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public String getName() {
        return name;
    }
}
