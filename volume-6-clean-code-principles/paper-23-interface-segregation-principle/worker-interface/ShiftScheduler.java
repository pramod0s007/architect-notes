import java.util.List;

/**
 * ShiftScheduler — assigns work tasks to a mixed team of humans and robots.
 *
 * Programs only to the Workable interface; no instanceof checks; no knowledge
 * of whether a given worker is human or robotic.  Adding a new worker type
 * (e.g. drone, AI agent) requires zero changes here as long as it implements Workable.
 */
public class ShiftScheduler {

    /**
     * Start a shift for all workers in the team.
     *
     * @param workers any Workable — humans, robots, or future types
     */
    public void scheduleShift(List<Workable> workers) {
        System.out.println("-- Shift starting: " + workers.size() + " worker(s) --");
        double totalCost = 0;
        for (Workable worker : workers) {
            if (!worker.isOnBreak()) {
                worker.work();
                totalCost += worker.getHourlyRate();
            } else {
                System.out.println("  (worker on break — skipped)");
            }
        }
        System.out.printf("-- Shift hour cost: $%.2f --%n", totalCost);
    }
}
