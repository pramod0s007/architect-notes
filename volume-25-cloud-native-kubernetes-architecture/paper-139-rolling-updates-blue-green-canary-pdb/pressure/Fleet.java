/**
 * Models a Deployment's fleet of replicas as a single available-replica count (no per-pod
 * detail needed for this simulation). takeDown() models any voluntary disruption removing
 * replicas -- a rolling-update wave replacing pods, or a node drain evicting pods for
 * maintenance. bringUp() models replacements becoming ready again.
 */
public class Fleet {
    private final int totalReplicas;
    private int availableReplicas;

    public Fleet(int totalReplicas) {
        this.totalReplicas = totalReplicas;
        this.availableReplicas = totalReplicas;
    }

    public int available() {
        return availableReplicas;
    }

    public int total() {
        return totalReplicas;
    }

    public void takeDown(int count) {
        availableReplicas = Math.max(0, availableReplicas - count);
    }

    public void bringUp(int count) {
        availableReplicas = Math.min(totalReplicas, availableReplicas + count);
    }
}
