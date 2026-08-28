public class Main {
    private static final int NODES = 5;

    public static void main(String[] args) {
        // Each app server runs its own perfectly-correct, in-memory TokenBucket.
        // Nobody shares state between them -- "rate limit = 100/sec" was a
        // global decision, enforced per-node instead.
        TokenBucket[] nodes = new TokenBucket[NODES];
        for (int i = 0; i < NODES; i++) {
            nodes[i] = new TokenBucket(100, 0.1, 0);
        }

        // A client (or a load balancer spreading traffic) hits all 5 nodes evenly.
        int totalAccepted = 0;
        for (TokenBucket node : nodes) {
            for (int i = 0; i < 100; i++) {
                if (node.allow(999)) totalAccepted++;
            }
        }

        System.out.println("Intended global limit: 100 requests/sec.");
        System.out.println("Nodes: " + NODES + ", each independently enforcing 100/sec locally.");
        System.out.println("Total accepted across all nodes: " + totalAccepted);
        System.out.println("The global limit is effectively " + totalAccepted + "/sec, not 100 -- " + NODES + "x over budget.");
    }
}
