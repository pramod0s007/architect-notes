/**
 * RobotWorkerFixed — autonomous assembly robot, ISP-compliant.
 *
 * Implements Workable only — the compiler ensures it is never asked to eat
 * or sleep.  No UnsupportedOperationException, no fake method bodies.
 */
public class RobotWorkerFixed implements Workable {

    private final String unitId;
    private final double hourlyRate;

    public RobotWorkerFixed(String unitId, double hourlyRate) {
        this.unitId     = unitId;
        this.hourlyRate = hourlyRate;
    }

    @Override public void work() {
        System.out.printf("RobotFixed %s: executing assembly sequence%n", unitId);
    }

    @Override public double getHourlyRate() { return hourlyRate; }

    // Robots never take breaks — this is a truthful return value, not a workaround
    @Override public boolean isOnBreak()    { return false;      }
}
