import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WRONG DIRECTION: teams now tag their pods, and the chargeback report splits each node's cost
 * evenly across the pods running on it -- one pod, one equal share of the node's cost, regardless
 * of team. This "fixes" the pressure scenario's total absence of attribution, but a pod is not a
 * fixed unit of resource consumption: a pod count share assumes every pod on a node used the same
 * amount of CPU and memory, which is almost never true.
 */
public class Main {
    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("node-1", 900.00, List.of(
                new Pod("checkout", 900.0, 3.6),
                new Pod("search", 50.0, 0.5),
                new Pod("search", 50.0, 0.5),
                new Pod("recommendations", 50.0, 0.4)
        )));

        Map<String, Double> chargebackByPodCount = new LinkedHashMap<>();
        Map<String, Double> actualUsageShare = new LinkedHashMap<>();

        for (Node n : nodes) {
            int podCount = n.pods.size();
            double costPerPod = n.monthlyCost / podCount;

            double totalCpuSeconds = 0.0;
            for (Pod p : n.pods) {
                totalCpuSeconds += p.cpuSeconds;
            }

            for (Pod p : n.pods) {
                chargebackByPodCount.merge(p.team, costPerPod, Double::sum);
                double usageShareCost = n.monthlyCost * (p.cpuSeconds / totalCpuSeconds);
                actualUsageShare.merge(p.team, usageShareCost, Double::sum);
            }
        }

        System.out.println("-- Chargeback report: cost split evenly by pod count on each node --");
        double chargedTotal = 0.0;
        for (Map.Entry<String, Double> e : chargebackByPodCount.entrySet()) {
            System.out.printf("  %s: $%.2f%n", e.getKey(), e.getValue());
            chargedTotal += e.getValue();
        }
        System.out.printf("  Total charged: $%.2f%n", chargedTotal);

        System.out.println();
        System.out.println("-- What each team actually used, measured in CPU-seconds on node-1 --");
        for (Node n : nodes) {
            for (Pod p : n.pods) {
                System.out.printf("  %s pod: %.1f CPU-seconds%n", p.team, p.cpuSeconds);
            }
        }

        System.out.println();
        System.out.println("-- What a usage-proportional charge would have been, for comparison --");
        for (Map.Entry<String, Double> e : actualUsageShare.entrySet()) {
            System.out.printf("  %s: $%.2f%n", e.getKey(), e.getValue());
        }

        System.out.println();
        System.out.println("Result: checkout's single pod used 900 of the node's 1050 total measured");
        System.out.println("CPU-seconds (85.7%) but is only charged $225.00 (25%) under the pod-count");
        System.out.println("split, because it happens to share the node with three other pods. Search");
        System.out.println("is charged $450.00 for two pods that together used only 9.5% of the node's");
        System.out.println("measured CPU-seconds. The pod-count chargeback total ($900.00) matches the");
        System.out.println("node's real bill, so it appears to reconcile at the total level -- but it moves");
        System.out.println("allocated cost away from the team with the heaviest measured usage, toward");
        System.out.println("teams that happened to run more, lighter pods on the same shared node.");
    }

    static class Pod {
        String team;
        double cpuSeconds;
        double memGbSeconds;

        Pod(String team, double cpuSeconds, double memGbSeconds) {
            this.team = team;
            this.cpuSeconds = cpuSeconds;
            this.memGbSeconds = memGbSeconds;
        }
    }

    static class Node {
        String name;
        double monthlyCost;
        List<Pod> pods;

        Node(String name, double monthlyCost, List<Pod> pods) {
            this.name = name;
            this.monthlyCost = monthlyCost;
            this.pods = pods;
        }
    }
}
