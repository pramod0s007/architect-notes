/** A pod that takes several ticks to finish warming up (loading a large in-memory cache). */
public class Pod {
    private int warmupTicksRemaining;

    public Pod(int warmupTicks) {
        this.warmupTicksRemaining = warmupTicks;
    }

    public void tick() {
        if (warmupTicksRemaining > 0) {
            warmupTicksRemaining--;
        }
    }

    public boolean isWarmedUp() {
        return warmupTicksRemaining == 0;
    }

    public void restart(int warmupTicks) {
        warmupTicksRemaining = warmupTicks;
    }
}
