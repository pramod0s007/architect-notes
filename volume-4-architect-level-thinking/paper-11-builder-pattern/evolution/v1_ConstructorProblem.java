package evolution;

import java.util.List;

/**
 * EVOLUTION v1 — Constructor Telescope Problem
 *
 * Domain: Alert Configuration (monitoring alerts)
 *
 * Problem: AlertConfig is created with an 8-parameter constructor.
 * A caller accidentally passes `true` (boolean) for the `priority` int
 * parameter. In Java, boolean cannot be implicitly promoted to int, but
 * the realistic production accident is reversed argument order:
 * the caller swaps `maxRetries` and `priority`, or passes `isActive`
 * as a literal `1` into the wrong slot — the compiler cannot catch it.
 *
 * The demonstrated bug: constructor argument order confusion.
 * thresholdValue=100, metricName="cpu_usage", channelType="email",
 * recipients are correct — but the caller writes priority=5, isActive=true
 * in the wrong order, swapping priority and windowSeconds silently.
 * The alert fires for a 5-second window instead of 300, and priority=1
 * instead of 5. Production alerts go unnoticed.
 */
public class v1_ConstructorProblem {

    // ---------------------------------------------------------------
    // Domain object — all-args constructor, 8 parameters
    // ---------------------------------------------------------------
    static class AlertConfig {
        private final String  metricName;
        private final double  thresholdValue;
        private final String  channelType;      // "email", "pagerduty", "slack"
        private final List<String> recipients;
        private final int     priority;         // 1=low … 5=critical
        private final int     windowSeconds;    // evaluation window
        private final int     maxRetries;
        private final boolean isActive;

        /**
         * Eight-parameter constructor.
         * Caller must remember exact order: metric, threshold, channel,
         * recipients, priority, windowSeconds, maxRetries, isActive.
         */
        public AlertConfig(
                String metricName,
                double thresholdValue,
                String channelType,
                List<String> recipients,
                int priority,
                int windowSeconds,
                int maxRetries,
                boolean isActive) {
            this.metricName     = metricName;
            this.thresholdValue = thresholdValue;
            this.channelType    = channelType;
            this.recipients     = recipients;
            this.priority       = priority;
            this.windowSeconds  = windowSeconds;
            this.maxRetries     = maxRetries;
            this.isActive       = isActive;
        }

        @Override
        public String toString() {
            return "AlertConfig{metric='" + metricName + "', threshold=" + thresholdValue
                    + ", channel='" + channelType + "', priority=" + priority
                    + ", windowSeconds=" + windowSeconds + ", maxRetries=" + maxRetries
                    + ", isActive=" + isActive + ", recipients=" + recipients + "}";
        }
    }

    // ---------------------------------------------------------------
    // Callers — one correct, two with silent bugs
    // ---------------------------------------------------------------
    static class AlertConfigurationService {

        /** Correct usage — developer had the docs open. */
        AlertConfig createCpuAlert() {
            return new AlertConfig(
                    "cpu_usage",        // metricName
                    80.0,               // thresholdValue
                    "pagerduty",        // channelType
                    List.of("oncall@corp.com"),
                    5,                  // priority  (critical)
                    300,                // windowSeconds
                    3,                  // maxRetries
                    true                // isActive
            );
        }

        /**
         * BUG: developer swapped priority and windowSeconds.
         * priority=300 (meaningless) and windowSeconds=5 (too narrow).
         * Compiler is silent. Alert fires after 5-second spikes only,
         * misses sustained incidents. Discovered in production at 2 AM.
         */
        AlertConfig createMemoryAlert() {
            return new AlertConfig(
                    "memory_usage",
                    90.0,
                    "email",
                    List.of("platform@corp.com", "sre@corp.com"),
                    300,                // BUG: this is windowSeconds, not priority
                    5,                  // BUG: this is priority, not windowSeconds
                    3,
                    true
            );
        }

        /**
         * BUG: developer added a new recipient list but forgot maxRetries,
         * shifted all int args right by one position. isActive ends up as
         * the value for maxRetries, which does not compile for boolean→int...
         * so the real-world accident is: developer passes 1 instead of true,
         * forgets to set isActive=true, alert is silently inactive.
         */
        AlertConfig createDiskAlert() {
            return new AlertConfig(
                    "disk_usage",
                    95.0,
                    "slack",
                    List.of("storage@corp.com"),
                    4,                  // priority — OK
                    600,                // windowSeconds — OK
                    2,                  // maxRetries — OK
                    false               // BUG: forgot to flip to true; alert never fires
            );
        }
    }

    // ---------------------------------------------------------------
    // Main — demonstrate the silent bugs
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        var service = new AlertConfigurationService();

        System.out.println("=== v1: Constructor Telescope Problem ===\n");

        AlertConfig cpu = service.createCpuAlert();
        System.out.println("CPU alert (correct):    " + cpu);

        AlertConfig memory = service.createMemoryAlert();
        System.out.println("Memory alert (BUGGY):   " + memory);
        System.out.println("  -> priority=" + memory.priority + " (should be 5, got 300)");
        System.out.println("  -> windowSeconds=" + memory.windowSeconds + " (should be 300, got 5)");

        AlertConfig disk = service.createDiskAlert();
        System.out.println("Disk alert (inactive):  " + disk);
        System.out.println("  -> isActive=" + disk.isActive + " (alert never fires in production)");

        System.out.println("\nAll three compile cleanly. Zero compiler warnings.");
        System.out.println("The bugs survive code review because reviewers read intent, not positions.");
    }
}
