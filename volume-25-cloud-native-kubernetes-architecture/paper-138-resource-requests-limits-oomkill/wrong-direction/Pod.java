/**
 * A pod's process memory usage, which leaks (grows) by a fixed rate each tick since its
 * last restart. Simulation: if a memory limit is configured (limitMB > 0) and usage
 * exceeds it, we model the kernel's cgroup OOM killer terminating the container and
 * kubelet restarting it (the container's restartPolicy determines this in real
 * Kubernetes), with usage resetting to baseline. With no limit configured
 * (limitMB <= 0), usage grows unchecked.
 */
public class Pod {
    private final int baselineMB;
    private final int leakRateMBPerTick;
    private final int limitMB;
    private int usageMB;
    private int restartCount;

    public Pod(int baselineMB, int leakRateMBPerTick, int limitMB) {
        this.baselineMB = baselineMB;
        this.leakRateMBPerTick = leakRateMBPerTick;
        this.limitMB = limitMB;
        this.usageMB = baselineMB;
        this.restartCount = 0;
    }

    /** Advances one tick; returns true if this tick's growth triggered an OOMKill+restart. */
    public boolean tick() {
        usageMB += leakRateMBPerTick;
        if (limitMB > 0 && usageMB > limitMB) {
            usageMB = baselineMB;
            restartCount++;
            return true;
        }
        return false;
    }

    public int usageMB() {
        return usageMB;
    }

    public int restartCount() {
        return restartCount;
    }
}
