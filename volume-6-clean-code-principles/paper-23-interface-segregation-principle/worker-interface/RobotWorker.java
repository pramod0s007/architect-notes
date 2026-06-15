/**
 * RobotWorker — an autonomous assembly robot.
 *
 * // Robots don't eat or sleep — forced by fat interface.
 * Both biological methods throw UnsupportedOperationException.
 * Any code that schedules lunch breaks across a List<Worker>
 * will crash at runtime when it reaches a robot.
 */
public class RobotWorker implements Worker {

    private final String unitId;
    private final double hourlyRate;

    public RobotWorker(String unitId, double hourlyRate) {
        this.unitId     = unitId;
        this.hourlyRate = hourlyRate;
    }

    @Override public void work() {
        System.out.printf("Robot %s: executing assembly sequence%n", unitId);
    }

    // ISP violation: robots don't eat — forced method throws
    @Override public void eat() {
        throw new UnsupportedOperationException("Robots do not eat");
    }

    // ISP violation: robots don't sleep — forced method throws
    @Override public void sleep() {
        throw new UnsupportedOperationException("Robots do not sleep");
    }

    @Override public double getHourlyRate() { return hourlyRate; }

    // Robots never take breaks
    @Override public boolean isOnBreak()    { return false;      }
}
