import java.util.ArrayList;
import java.util.List;

/**
 * Assigns each key to a node by plain modulo: hash(key) % nodeCount.
 * This gives a perfectly even distribution for a fixed node count, but
 * the node a key maps to depends on the total node count itself, so
 * adding or removing a single node changes the divisor for every key,
 * not just the keys that "belong" near the new node.
 */
public class ModuloPartitioner {
    private final List<String> nodes;

    public ModuloPartitioner(List<String> nodes) {
        this.nodes = new ArrayList<>(nodes);
    }

    public String assign(String key) {
        long h = Hashing.hash(key);
        int index = (int) (h % nodes.size());
        return nodes.get(index);
    }
}
