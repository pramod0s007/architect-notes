import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A consistent-hashing ring where each physical node contributes many
 * virtual points instead of just one. Spreading a node's ownership across
 * thousands of small, scattered arcs (rather than one large contiguous
 * arc) is what actually balances load -- the law of large numbers evens
 * out the per-node total even though any single arc is still essentially
 * random in size. Resize behavior is unchanged from the single-point
 * ring: only the keys whose nearest ring point moves are affected.
 */
public class VirtualNodeRing {
    public static final int VIRTUAL_POINTS_PER_NODE = 2000;

    private final NavigableMap<Long, String> ring = new TreeMap<>();

    public void addNode(String node) {
        for (int i = 0; i < VIRTUAL_POINTS_PER_NODE; i++) {
            ring.put(Hashing.hash(node + "-vnode-" + i), node);
        }
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
