import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A consistent-hashing ring with exactly one point per physical node.
 * A key is assigned to the first node whose ring position is greater than
 * or equal to the key's own hash (wrapping around to the smallest position
 * if the key's hash is past every node). Because only the ring positions
 * between a resized node and its neighbor are affected by adding or
 * removing that node, most keys keep their existing assignment across a
 * resize -- unlike modulo partitioning, where the divisor changes for
 * every key at once.
 */
public class ConsistentHashRing {
    private final NavigableMap<Long, String> ring = new TreeMap<>();

    public void addNode(String node) {
        ring.put(Hashing.hash(node), node);
    }

    public void removeNode(String node) {
        ring.entrySet().removeIf(e -> e.getValue().equals(node));
    }

    public String assign(String key) {
        long h = Hashing.hash(key);
        Map.Entry<Long, String> entry = ring.ceilingEntry(h);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }
}
