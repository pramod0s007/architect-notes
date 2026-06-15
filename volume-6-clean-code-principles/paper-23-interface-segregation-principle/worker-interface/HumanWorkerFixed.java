/**
 * HumanWorkerFixed — human employee, ISP-compliant.
 * Implements both Workable (operational) and BiologicalNeeds (life-cycle).
 */
public class HumanWorkerFixed implements Workable, BiologicalNeeds {

    private final String name;
    private final double hourlyRate;
    private boolean onBreak = false;

    public HumanWorkerFixed(String name, double hourlyRate) {
        this.name       = name;
        this.hourlyRate = hourlyRate;
    }

    @Override public void work() {
        onBreak = false;
        System.out.printf("%s: working on assigned tasks%n", name);
    }

    @Override public void eat() {
        onBreak = true;
        System.out.printf("%s: having lunch%n", name);
    }

    @Override public void sleep() {
        onBreak = true;
        System.out.printf("%s: resting during break%n", name);
    }

    @Override public double getHourlyRate() { return hourlyRate; }

    @Override public boolean isOnBreak()    { return onBreak;    }
}
