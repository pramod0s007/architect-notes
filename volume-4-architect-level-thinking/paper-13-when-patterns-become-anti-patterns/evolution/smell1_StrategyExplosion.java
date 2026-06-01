package evolution;

import java.time.LocalTime;
import java.util.Map;

/**
 * SMELL 1 — Strategy Explosion (Pattern Applied to Data, Not Behavior)
 *
 * Anti-Pattern: GreetingStrategy interface with one method each for
 * Morning, Afternoon, and Evening — 3 files to return a string literal.
 *
 * The Strategy Pattern is for varying ALGORITHMS (complex behavior).
 * "Return a different string" is not an algorithm — it is data.
 * Using Strategy here creates 4 files (interface + 3 impls) where a
 * Map<TimeOfDay, String> would do the same job in 6 lines.
 *
 * Fix: Replace interface hierarchy with Map lookup.
 */
public class smell1_StrategyExplosion {

    // ---------------------------------------------------------------
    // BEFORE: Strategy explosion — 3 classes for 3 string literals
    // ---------------------------------------------------------------

    enum TimeOfDay { MORNING, AFTERNOON, EVENING }

    /** Interface — one method, returns a string. No complex algorithm. */
    interface GreetingStrategy {
        String greet(String userName);
    }

    /** Entire class exists to return "Good morning". */
    static class MorningGreeting implements GreetingStrategy {
        @Override
        public String greet(String userName) {
            return "Good morning, " + userName + "!";
        }
    }

    /** Entire class exists to return "Good afternoon". */
    static class AfternoonGreeting implements GreetingStrategy {
        @Override
        public String greet(String userName) {
            return "Good afternoon, " + userName + "!";
        }
    }

    /** Entire class exists to return "Good evening". */
    static class EveningGreeting implements GreetingStrategy {
        @Override
        public String greet(String userName) {
            return "Good evening, " + userName + "!";
        }
    }

    /** Factory needed just to pick the right greeting. */
    static class GreetingStrategyFactory {
        static GreetingStrategy forTimeOfDay(TimeOfDay time) {
            return switch (time) {
                case MORNING   -> new MorningGreeting();
                case AFTERNOON -> new AfternoonGreeting();
                case EVENING   -> new EveningGreeting();
            };
        }
    }

    /** Service that uses the strategy. */
    static class GreetingService_Before {
        String greet(String userName, TimeOfDay time) {
            GreetingStrategy strategy = GreetingStrategyFactory.forTimeOfDay(time);
            return strategy.greet(userName);
            // 4 files. 1 factory. For returning a string.
        }
    }

    // ---------------------------------------------------------------
    // AFTER: Map lookup — data is data, not behavior
    // ---------------------------------------------------------------

    static class GreetingService_After {
        private static final Map<TimeOfDay, String> TEMPLATES = Map.of(
                TimeOfDay.MORNING,   "Good morning, %s!",
                TimeOfDay.AFTERNOON, "Good afternoon, %s!",
                TimeOfDay.EVENING,   "Good evening, %s!"
        );

        String greet(String userName, TimeOfDay time) {
            String template = TEMPLATES.getOrDefault(time, "Hello, %s!");
            return String.format(template, userName);
            // 1 method. 1 Map. Same result. Adding a new greeting = 1 line.
        }
    }

    // ---------------------------------------------------------------
    // Main — demonstrate both approaches, same output
    // ---------------------------------------------------------------
    static TimeOfDay currentTimeOfDay() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return TimeOfDay.MORNING;
        if (hour < 18) return TimeOfDay.AFTERNOON;
        return TimeOfDay.EVENING;
    }

    public static void main(String[] args) {
        String user    = "Alice";
        TimeOfDay time = TimeOfDay.MORNING;

        System.out.println("=== Smell 1: Strategy Explosion ===\n");

        System.out.println("--- BEFORE: 4 files, 1 factory ---");
        GreetingService_Before before = new GreetingService_Before();
        System.out.println(before.greet(user, TimeOfDay.MORNING));
        System.out.println(before.greet(user, TimeOfDay.AFTERNOON));
        System.out.println(before.greet(user, TimeOfDay.EVENING));

        System.out.println();
        System.out.println("--- AFTER: 1 Map ---");
        GreetingService_After after = new GreetingService_After();
        System.out.println(after.greet(user, TimeOfDay.MORNING));
        System.out.println(after.greet(user, TimeOfDay.AFTERNOON));
        System.out.println(after.greet(user, TimeOfDay.EVENING));

        System.out.println();
        System.out.println("Diagnosis: Strategy is for ALGORITHMS that vary.");
        System.out.println("If the only thing varying is a string/value — use a Map.");
        System.out.println("Ask: 'Would I unit-test each class independently?' If no -> not Strategy.");
    }
}
