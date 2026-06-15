import java.util.Arrays;
import java.util.List;

/** Demos the fat Worker ISP violation with robots, then a clean mixed-team schedule. */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== VIOLATION: Fat Worker interface ===");
        Worker human = new HumanWorker("Alice", 45.00);
        Worker robot = new RobotWorker("R2-D1", 12.00);

        human.work();
        human.eat();
        robot.work();

        System.out.println("Calling robot.eat():");
        try { robot.eat(); }
        catch (UnsupportedOperationException e) { System.out.println("  " + e.getMessage()); }

        System.out.println("Calling robot.sleep():");
        try { robot.sleep(); }
        catch (UnsupportedOperationException e) { System.out.println("  " + e.getMessage()); }

        System.out.println();
        System.out.println("=== FIX: Workable + BiologicalNeeds ===");
        HumanWorkerFixed alice = new HumanWorkerFixed("Alice", 45.00);
        HumanWorkerFixed bob   = new HumanWorkerFixed("Bob",   40.00);
        RobotWorkerFixed r2d1  = new RobotWorkerFixed("R2-D1", 12.00);
        RobotWorkerFixed r2d2  = new RobotWorkerFixed("R2-D2", 12.00);

        // Biological needs — only for humans; robots can't be passed here
        System.out.println("Lunch break:");
        for (HumanWorkerFixed h : Arrays.asList(alice, bob)) { h.eat(); }

        // Mixed-team shift — no instanceof; works for humans and robots alike
        ShiftScheduler scheduler = new ShiftScheduler();
        List<Workable> team = Arrays.asList(alice, bob, r2d1, r2d2);
        scheduler.scheduleShift(team);

        System.out.println("RobotFixed has no eat() or sleep() methods at all.");
    }
}
