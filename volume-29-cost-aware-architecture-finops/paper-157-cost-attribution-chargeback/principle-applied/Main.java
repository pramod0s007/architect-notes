import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PRINCIPLE APPLIED: attribute each node's cost by the actual resource dimensions its price
 * reflects, not by pod count and not by a single resource dimension either. A node's price is
 * modeled here as split across a CPU-priced portion and a memory-priced portion, using a fixed,
 * disclosed allocation policy (70% CPU / 30% memory) rather than a rate genuinely derived from
 * any specific cloud provider's billing. Each portion is then split among the pods that actually
 * ran on that node, proportional to that pod's measured usage of that specific resource --
 * CPU-seconds for the CPU portion, GB-seconds for the memory portion -- and the two portions are
 * summed per team. This catches what a CPU-seconds-only usage split (a plausible next fix after
 * the pod-count mistake) would still miss: a pod that is light on CPU but heavy on memory gets a
 * meaningfully larger share of the bill once memory is its own priced, separately-attributed
 * dimension. If a node reports zero measured usage in one of the two dimensions -- an idle,
 * provisioned-but-unused node is a real case, not a hypothetical -- that dimension's portion is
 * explicitly assigned to an "unallocated (idle capacity)" bucket rather than silently dividing by
 * zero, so idle spend stays visible instead of disappearing or crashing the report.
 */
public class Main {
    private static final double CPU_PRICE_SHARE = 0.70;
    private static final double MEMORY_PRICE_SHARE = 0.30;
    private static final String IDLE_CAPACITY_BUCKET = "unallocated (idle capacity)";

    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<>();

        // node-1: shared by three teams, with recommendations deliberately light on CPU but heavy
        // on memory (it holds a large in-memory embedding cache) -- this is the case a CPU-only
        // usage split would still get wrong.
        nodes.add(new Node("node-1", 900.00, List.of(
                new Pod("checkout", 900.0, 3.6),
                new Pod("search", 50.0, 0.5),
                new Pod("search", 50.0, 0.5),
                new Pod("recommendations", 50.0, 40.0)
        )));

        // node-2: dedicated entirely to search -- a single-tenant node. Whatever the usage split
        // computes, search should end up owning 100% of this node's cost, even though its one pod
        // doesn't use the node's full capacity -- idle, paid-for capacity on a dedicated node is
        // still that team's cost to own and right-size.
        nodes.add(new Node("node-2", 900.00, List.of(
                new Pod("search", 800.0, 5.0)
        )));

        // node-3: dedicated entirely to recommendations, same single-tenant reasoning.
        nodes.add(new Node("node-3", 900.00, List.of(
                new Pod("recommendations", 10.0, 2.0)
        )));

        // node-4: provisioned but genuinely idle for this billing period -- its one pod reports
        // zero measured CPU-seconds and zero GB-seconds (e.g., scaled down mid-period, or a
        // metrics gap). Both dimensions have a zero total, so both portions go to the idle bucket
        // instead of producing a divide-by-zero.
        nodes.add(new Node("node-4", 300.00, List.of(
                new Pod("platform", 0.0, 0.0)
        )));

        Map<String, Double> chargeback = new LinkedHashMap<>();
        double totalBill = 0.0;

        for (Node n : nodes) {
            totalBill += n.monthlyCost;
            double cpuPortion = n.monthlyCost * CPU_PRICE_SHARE;
            double memPortion = n.monthlyCost * MEMORY_PRICE_SHARE;

            double totalCpuSeconds = 0.0;
            double totalMemGbSeconds = 0.0;
            for (Pod p : n.pods) {
                totalCpuSeconds += p.cpuSeconds;
                totalMemGbSeconds += p.memGbSeconds;
            }

            if (totalCpuSeconds == 0.0) {
                chargeback.merge(IDLE_CAPACITY_BUCKET, cpuPortion, Double::sum);
            }
            if (totalMemGbSeconds == 0.0) {
                chargeback.merge(IDLE_CAPACITY_BUCKET, memPortion, Double::sum);
            }

            for (Pod p : n.pods) {
                double cpuShare = totalCpuSeconds == 0.0 ? 0.0 : cpuPortion * (p.cpuSeconds / totalCpuSeconds);
                double memShare = totalMemGbSeconds == 0.0 ? 0.0 : memPortion * (p.memGbSeconds / totalMemGbSeconds);
                chargeback.merge(p.team, cpuShare + memShare, Double::sum);
            }
        }

        System.out.println("-- Chargeback report: cost split by priced resource dimension, by actual usage --");
        double chargedTotal = 0.0;
        for (Map.Entry<String, Double> e : chargeback.entrySet()) {
            System.out.printf("  %s: $%.2f%n", e.getKey(), e.getValue());
            chargedTotal += e.getValue();
        }
        System.out.printf("  Total charged: $%.2f%n", chargedTotal);
        System.out.printf("  Total bill: $%.2f%n", totalBill);
        System.out.printf("  Reconciled (charged == bill, within rounding): %s%n",
                Math.abs(chargedTotal - totalBill) < 0.01);

        System.out.println();
        System.out.println("-- Compare: what a CPU-seconds-only usage split would have charged recommendations on node-1 --");
        Node node1 = nodes.get(0);
        double node1CpuTotal = 0.0;
        double node1MemTotal = 0.0;
        for (Pod p : node1.pods) {
            node1CpuTotal += p.cpuSeconds;
            node1MemTotal += p.memGbSeconds;
        }
        double recsCpuSeconds = 0.0;
        double recsMemGbSeconds = 0.0;
        for (Pod p : node1.pods) {
            if (p.team.equals("recommendations")) {
                recsCpuSeconds += p.cpuSeconds;
                recsMemGbSeconds += p.memGbSeconds;
            }
        }
        double node1RecsCpuOnlyShare = node1.monthlyCost * (recsCpuSeconds / node1CpuTotal);
        double node1RecsCpuPortionShare = node1.monthlyCost * CPU_PRICE_SHARE * (recsCpuSeconds / node1CpuTotal);
        double node1RecsMemPortionShare = node1.monthlyCost * MEMORY_PRICE_SHARE * (recsMemGbSeconds / node1MemTotal);
        double node1RecsDimensionalTotal = node1RecsCpuPortionShare + node1RecsMemPortionShare;
        System.out.printf("  recommendations on node-1, CPU-seconds-only method: $%.2f%n", node1RecsCpuOnlyShare);
        System.out.printf("  recommendations on node-1, CPU-priced-portion only (dimensional method): $%.2f%n",
                node1RecsCpuPortionShare);
        System.out.printf("  recommendations on node-1, memory-priced-portion only (dimensional method): $%.2f%n",
                node1RecsMemPortionShare);
        System.out.printf("  recommendations on node-1, CPU+memory dimensional total: $%.2f%n",
                node1RecsDimensionalTotal);

        System.out.println();
        System.out.println("Result: the dimensional split reconciles exactly to the real bill, gives");
        System.out.println("node-2 and node-3 entirely to their single tenants regardless of how");
        System.out.println("lightly those tenants used the node's capacity, and routes node-4's fully");
        System.out.println("idle capacity to its own visible bucket instead of dividing by zero. On the");
        System.out.println("shared node-1, the CPU-priced portion alone ($30.00) is smaller than the");
        System.out.println("naive CPU-seconds-only charge ($42.86), simply because it's now only 70% of");
        System.out.println("the node's cost -- but once recommendations' memory-priced portion ($242.15)");
        System.out.println("is added, its node-1 total ($272.15) is more than six times the naive");
        System.out.println("CPU-only estimate, because memory is where its real footprint actually is.");
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
