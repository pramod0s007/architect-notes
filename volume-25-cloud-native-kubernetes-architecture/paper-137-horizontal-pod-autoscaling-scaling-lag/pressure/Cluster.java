import java.util.ArrayList;
import java.util.List;

/** Tracks a fleet of pods, each becoming "ready" (counted toward capacity) startupLag ticks after being added. */
public class Cluster {
    private final List<Integer> readyAtTicks = new ArrayList<>();
    private final int capacityPerPod;

    public Cluster(int initialPods, int capacityPerPod) {
        this.capacityPerPod = capacityPerPod;
        for (int i = 0; i < initialPods; i++) {
            readyAtTicks.add(0); // already ready at tick 0
        }
    }

    public void addPods(int count, int currentTick, int startupLag) {
        for (int i = 0; i < count; i++) {
            readyAtTicks.add(currentTick + startupLag);
        }
    }

    public void removePods(int count) {
        for (int i = 0; i < count && !readyAtTicks.isEmpty(); i++) {
            readyAtTicks.remove(readyAtTicks.size() - 1);
        }
    }

    public int readyPodCount(int currentTick) {
        int count = 0;
        for (int r : readyAtTicks) {
            if (r <= currentTick) count++;
        }
        return count;
    }

    public int totalPodCount() {
        return readyAtTicks.size();
    }

    public int capacity(int currentTick) {
        return readyPodCount(currentTick) * capacityPerPod;
    }
}
