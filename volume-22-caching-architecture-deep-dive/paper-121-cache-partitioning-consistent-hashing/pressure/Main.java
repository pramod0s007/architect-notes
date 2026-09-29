import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates that plain modulo partitioning, while perfectly balanced
 * for a fixed node count, remaps almost every key when the node count
 * changes -- because every key's assignment depends on the divisor, not
 * just on where that specific key happens to fall.
 */
public class Main {
    public static void main(String[] args) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            keys.add("key-" + i);
        }

        List<String> fourNodes = Arrays.asList("nodeA", "nodeB", "nodeC", "nodeD");
        ModuloPartitioner before = new ModuloPartitioner(fourNodes);

        int[] countsBefore = new int[fourNodes.size()];
        String[] assignmentBefore = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            String node = before.assign(keys.get(i));
            assignmentBefore[i] = node;
            countsBefore[fourNodes.indexOf(node)]++;
        }
        System.out.println("4 nodes, key distribution: " + Arrays.toString(countsBefore));

        // A 5th node is added to handle more load.
        List<String> fiveNodes = Arrays.asList("nodeA", "nodeB", "nodeC", "nodeD", "nodeE");
        ModuloPartitioner after = new ModuloPartitioner(fiveNodes);

        int[] countsAfter = new int[fiveNodes.size()];
        int unchanged = 0;
        for (int i = 0; i < keys.size(); i++) {
            String node = after.assign(keys.get(i));
            countsAfter[fiveNodes.indexOf(node)]++;
            if (node.equals(assignmentBefore[i])) {
                unchanged++;
            }
        }
        System.out.println("5 nodes, key distribution: " + Arrays.toString(countsAfter));
        System.out.println("Keys out of " + keys.size() + " that kept the same node after adding nodeE: " + unchanged);
        System.out.println("Keys remapped to a different node: " + (keys.size() - unchanged));
    }
}
