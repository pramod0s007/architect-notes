import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates that plain consistent hashing (a single ring point per
 * physical node) genuinely fixes the mass-remap problem, but introduces a
 * different, equally real failure: with only a handful of ring points,
 * the arcs each node owns are far from equal in length, so key load
 * across nodes is badly skewed even though remapping on resize is cheap.
 */
public class Main {
    public static void main(String[] args) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            keys.add("key-" + i);
        }

        List<String> fourNodes = Arrays.asList("india", "pegasus", "echo", "quebec");
        ConsistentHashRing before = new ConsistentHashRing();
        for (String node : fourNodes) {
            before.addNode(node);
        }

        int[] countsBefore = new int[fourNodes.size()];
        String[] assignmentBefore = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            String node = before.assign(keys.get(i));
            assignmentBefore[i] = node;
            countsBefore[fourNodes.indexOf(node)]++;
        }
        int minBefore = Arrays.stream(countsBefore).min().getAsInt();
        int maxBefore = Arrays.stream(countsBefore).max().getAsInt();
        System.out.println("4 nodes " + fourNodes + ", key distribution: " + Arrays.toString(countsBefore));
        System.out.println("Load skew: min=" + minBefore + ", max=" + maxBefore
                + ", expected even share=" + (keys.size() / fourNodes.size())
                + ", max is " + String.format("%.1fx", (double) maxBefore / minBefore) + " min");

        // Add a 5th node.
        ConsistentHashRing after = new ConsistentHashRing();
        for (String node : fourNodes) {
            after.addNode(node);
        }
        after.addNode("juliet");

        List<String> fiveNodes = Arrays.asList("india", "pegasus", "echo", "quebec", "juliet");
        int[] countsAfter = new int[fiveNodes.size()];
        int unchanged = 0;
        for (int i = 0; i < keys.size(); i++) {
            String node = after.assign(keys.get(i));
            countsAfter[fiveNodes.indexOf(node)]++;
            if (node.equals(assignmentBefore[i])) {
                unchanged++;
            }
        }
        System.out.println("5 nodes " + fiveNodes + ", key distribution: " + Arrays.toString(countsAfter));
        System.out.println("Keys out of " + keys.size() + " that kept the same node after adding juliet: " + unchanged);
        System.out.println("Keys remapped to a different node: " + (keys.size() - unchanged));
    }
}
