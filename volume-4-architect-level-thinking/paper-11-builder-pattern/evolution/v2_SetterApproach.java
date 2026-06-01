package evolution;

import java.util.ArrayList;
import java.util.List;

/**
 * EVOLUTION v2 — Setter Approach (Mutable, Unenforceable Invariants)
 *
 * Domain: Alert Configuration (monitoring alerts)
 *
 * Improvement over v1: No positional confusion — fields are set by name.
 *
 * New problems introduced:
 * 1. Object is mutable — any code can change priority after creation.
 * 2. Invariants cannot be enforced — build() does not exist,
 *    so a half-configured alert can be stored and activated.
 * 3. Nothing stops using the object before it is fully configured.
 * 4. Thread safety is gone — two threads can interleave setters.
 */
public class v2_SetterApproach {

    // ---------------------------------------------------------------
    // Domain object — mutable, setter-based
    // ---------------------------------------------------------------
    static class AlertConfig {
        private String  metricName;
        private double  thresholdValue;
        private String  channelType;
        private List<String> recipients = new ArrayList<>();
        private int     priority;
        private int     windowSeconds;
        private int     maxRetries;
        private boolean isActive;

        // No-arg constructor required for setter pattern
        public AlertConfig() {}

        // --- setters ---
        public void setMetricName(String metricName)         { this.metricName = metricName; }
        public void setThresholdValue(double thresholdValue) { this.thresholdValue = thresholdValue; }
        public void setChannelType(String channelType)       { this.channelType = channelType; }
        public void setRecipients(List<String> recipients)   { this.recipients = recipients; }
        public void setPriority(int priority)                { this.priority = priority; }
        public void setWindowSeconds(int windowSeconds)      { this.windowSeconds = windowSeconds; }
        public void setMaxRetries(int maxRetries)            { this.maxRetries = maxRetries; }
        public void setIsActive(boolean isActive)            { this.isActive = isActive; }

        // --- getters ---
        public String  getMetricName()      { return metricName; }
        public double  getThresholdValue()  { return thresholdValue; }
        public String  getChannelType()     { return channelType; }
        public List<String> getRecipients() { return recipients; }
        public int     getPriority()        { return priority; }
        public int     getWindowSeconds()   { return windowSeconds; }
        public int     getMaxRetries()      { return maxRetries; }
        public boolean isActive()           { return isActive; }

        @Override
        public String toString() {
            return "AlertConfig{metric='" + metricName + "', threshold=" + thresholdValue
                    + ", channel='" + channelType + "', priority=" + priority
                    + ", windowSeconds=" + windowSeconds + ", maxRetries=" + maxRetries
                    + ", isActive=" + isActive + ", recipients=" + recipients + "}";
        }
    }

    // ---------------------------------------------------------------
    // Alert Registry — stores and activates alerts
    // ---------------------------------------------------------------
    static class AlertRegistry {
        private final List<AlertConfig> configs = new ArrayList<>();

        public void register(AlertConfig config) {
            // PROBLEM: No validation here. Half-configured alerts slip through.
            configs.add(config);
        }

        public void activateAll() {
            for (AlertConfig c : configs) {
                if (c.isActive()) {
                    System.out.println("  Activating: " + c.getMetricName()
                            + " via " + c.getChannelType());
                }
            }
        }

        public List<AlertConfig> getConfigs() { return configs; }
    }

    // ---------------------------------------------------------------
    // Service — creates and registers alerts
    // ---------------------------------------------------------------
    static class AlertConfigurationService {

        /**
         * PROBLEM 1: Half-configured alert stored and used.
         * Developer forgot to call setChannelType and setRecipients.
         * The object compiles, registers, and activates — with nulls.
         */
        AlertConfig createIncompleteAlert() {
            AlertConfig config = new AlertConfig();
            config.setMetricName("network_latency");
            config.setThresholdValue(500.0);
            // Forgot: config.setChannelType("slack");
            // Forgot: config.setRecipients(List.of("netops@corp.com"));
            config.setPriority(3);
            config.setWindowSeconds(60);
            config.setMaxRetries(2);
            config.setIsActive(true);   // alert "active" but channel is null
            return config;
        }

        /**
         * PROBLEM 2: Object mutated after being registered.
         * Another service upgrades priority for all critical metrics —
         * but it changes the shared object, affecting all registries
         * that hold a reference to it.
         */
        void demonstrateMutationAfterRegistration(AlertRegistry registry) {
            AlertConfig config = new AlertConfig();
            config.setMetricName("cpu_usage");
            config.setThresholdValue(80.0);
            config.setChannelType("pagerduty");
            config.setRecipients(List.of("oncall@corp.com"));
            config.setPriority(3);
            config.setWindowSeconds(300);
            config.setMaxRetries(3);
            config.setIsActive(true);

            registry.register(config);

            // Later, some other code "adjusts" the same object reference
            System.out.println("  Before mutation: priority=" + config.getPriority());
            config.setPriority(1);  // MUTATED after registration — registry now holds stale data
            System.out.println("  After mutation:  priority=" + config.getPriority()
                    + " (registry's stored object ALSO changed)");
        }

        /**
         * PROBLEM 3: threshold = 0.0 (default).
         * No validation possible — there is no build() step to check it.
         */
        AlertConfig createZeroThresholdAlert() {
            AlertConfig config = new AlertConfig();
            config.setMetricName("error_rate");
            // Forgot: config.setThresholdValue(...)  -> stays at 0.0 default
            config.setChannelType("email");
            config.setRecipients(List.of("dev@corp.com"));
            config.setPriority(4);
            config.setWindowSeconds(120);
            config.setMaxRetries(3);
            config.setIsActive(true);
            return config;  // threshold=0.0 → alert fires on EVERY metric tick
        }
    }

    // ---------------------------------------------------------------
    // Main — demonstrate the three failure modes
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        var service  = new AlertConfigurationService();
        var registry = new AlertRegistry();

        System.out.println("=== v2: Setter Approach Problems ===\n");

        // Problem 1: half-configured object
        AlertConfig incomplete = service.createIncompleteAlert();
        registry.register(incomplete);
        System.out.println("Incomplete alert registered: " + incomplete);
        System.out.println("  -> channelType='" + incomplete.getChannelType() + "' (null! will NPE at runtime)");

        System.out.println();

        // Problem 2: mutation after registration
        System.out.println("Mutation after registration:");
        service.demonstrateMutationAfterRegistration(registry);

        System.out.println();

        // Problem 3: zero threshold
        AlertConfig zeroThresh = service.createZeroThresholdAlert();
        registry.register(zeroThresh);
        System.out.println("Zero threshold alert: " + zeroThresh);
        System.out.println("  -> threshold=0.0, alert will fire continuously in production");

        System.out.println();
        System.out.println("All compile. All register. All await runtime failure.");
        System.out.println("None of these bugs are catchable before activation.");
    }
}
