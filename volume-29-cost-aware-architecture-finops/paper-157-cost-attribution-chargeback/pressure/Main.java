import java.util.ArrayList;
import java.util.List;

/**
 * PRESSURE: three teams share a single Kubernetes cluster made of a handful of nodes. The cloud
 * bill arrives as one lump sum per node, with no per-team breakdown at all -- exactly how a
 * shared cluster's bill looks before anyone adds cost attribution. When the monthly bill spikes,
 * there is no way to tell which team's workload drove the increase.
 */
public class Main {
    public static void main(String[] args) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("node-1", 900.00));
        nodes.add(new Node("node-2", 900.00));
        nodes.add(new Node("node-3", 900.00));

        double totalBill = 0.0;
        for (Node n : nodes) {
            totalBill += n.monthlyCost;
        }

        System.out.println("-- Monthly cloud bill, no cost attribution --");
        for (Node n : nodes) {
            System.out.printf("  %s: $%.2f%n", n.name, n.monthlyCost);
        }
        System.out.printf("  Total bill: $%.2f%n", totalBill);
        System.out.println();
        System.out.println("Three teams (checkout, search, recommendations) all run pods on these");
        System.out.println("three nodes. The bill is one number per node. There is no field, tag,");
        System.out.println("or record anywhere that says which team's pods ran on which node, or");
        System.out.println("how much of each node's capacity each team's pods actually used.");
        System.out.println();
        System.out.println("Result: this month's total is $" + String.format("%.2f", totalBill)
                + ", up from $2,100.00 last month. Finance asks which team caused the increase.");
        System.out.println("There is no way to answer that question from this bill -- the increase");
        System.out.println("could be any of the three teams, in any proportion, and nothing here");
        System.out.println("can distinguish between those possibilities.");
    }

    static class Node {
        String name;
        double monthlyCost;

        Node(String name, double monthlyCost) {
            this.name = name;
            this.monthlyCost = monthlyCost;
        }
    }
}
