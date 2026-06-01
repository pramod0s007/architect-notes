package evolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EVOLUTION v3 — Builder Pattern Applied
 *
 * Domain: Alert Configuration (monitoring alerts)
 *
 * What changes from v1 and v2:
 * - AlertConfig is immutable (all fields final, no setters).
 * - Builder.build() validates all invariants before creating the object.
 * - Named steps eliminate positional confusion.
 * - Object cannot exist in a half-configured state.
 *
 * Design decisions in the builder:
 * - Required fields: metricName, thresholdValue, channelType, recipients
 * - Optional with defaults: priority=3, windowSeconds=300, maxRetries=3, isActive=true
 * - build() rejects: threshold <= 0, empty recipients, null channel
 */
public class v3_BuilderApplied {

    // ---------------------------------------------------------------
    // Immutable domain object — no public constructor
    // ---------------------------------------------------------------
    static final class AlertConfig {
        private final String       metricName;
        private final double       thresholdValue;
        private final String       channelType;
        private final List<String> recipients;
        private final int          priority;
        private final int          windowSeconds;
        private final int          maxRetries;
        private final boolean      isActive;

        // Private constructor — only Builder can create instances
        private AlertConfig(Builder b) {
            this.metricName     = b.metricName;
            this.thresholdValue = b.thresholdValue;
            this.channelType    = b.channelType;
            this.recipients     = Collections.unmodifiableList(new ArrayList<>(b.recipients));
            this.priority       = b.priority;
            this.windowSeconds  = b.windowSeconds;
            this.maxRetries     = b.maxRetries;
            this.isActive       = b.isActive;
        }

        // --- read-only getters ---
        public String       getMetricName()     { return metricName; }
        public double       getThresholdValue() { return thresholdValue; }
        public String       getChannelType()    { return channelType; }
        public List<String> getRecipients()     { return recipients; }
        public int          getPriority()       { return priority; }
        public int          getWindowSeconds()  { return windowSeconds; }
        public int          getMaxRetries()     { return maxRetries; }
        public boolean      isActive()          { return isActive; }

        @Override
        public String toString() {
            return "AlertConfig{metric='" + metricName + "', threshold=" + thresholdValue
                    + ", channel='" + channelType + "', priority=" + priority
                    + ", window=" + windowSeconds + "s, retries=" + maxRetries
                    + ", active=" + isActive + ", recipients=" + recipients + "}";
        }

        // ---------------------------------------------------------------
        // Builder — inner static class
        // ---------------------------------------------------------------
        static final class Builder {
            // Required — no defaults
            private String       metricName;
            private double       thresholdValue = -1; // sentinel for "not set"
            private String       channelType;
            private List<String> recipients = new ArrayList<>();

            // Optional — sensible defaults
            private int     priority      = 3;    // medium
            private int     windowSeconds = 300;  // 5-minute window
            private int     maxRetries    = 3;
            private boolean isActive      = true;

            public Builder metricName(String metricName) {
                this.metricName = metricName;
                return this;
            }

            public Builder threshold(double value) {
                this.thresholdValue = value;
                return this;
            }

            public Builder channel(String channelType) {
                this.channelType = channelType;
                return this;
            }

            public Builder recipients(List<String> recipients) {
                this.recipients = new ArrayList<>(recipients);
                return this;
            }

            public Builder addRecipient(String recipient) {
                this.recipients.add(recipient);
                return this;
            }

            public Builder priority(int priority) {
                this.priority = priority;
                return this;
            }

            public Builder windowSeconds(int seconds) {
                this.windowSeconds = seconds;
                return this;
            }

            public Builder maxRetries(int retries) {
                this.maxRetries = retries;
                return this;
            }

            public Builder inactive() {
                this.isActive = false;
                return this;
            }

            /**
             * build() is the only path to an AlertConfig instance.
             * All invariants are checked here — no invalid object can exist.
             */
            public AlertConfig build() {
                List<String> errors = new ArrayList<>();

                if (metricName == null || metricName.isBlank()) {
                    errors.add("metricName is required");
                }
                if (thresholdValue <= 0) {
                    errors.add("threshold must be > 0 (got " + thresholdValue + ")");
                }
                if (channelType == null || channelType.isBlank()) {
                    errors.add("channelType is required (email | slack | pagerduty)");
                }
                if (recipients.isEmpty()) {
                    errors.add("at least one recipient is required");
                }
                if (priority < 1 || priority > 5) {
                    errors.add("priority must be 1-5 (got " + priority + ")");
                }
                if (windowSeconds <= 0) {
                    errors.add("windowSeconds must be > 0");
                }

                if (!errors.isEmpty()) {
                    throw new IllegalStateException(
                            "AlertConfig.Builder.build() failed validation:\n  - "
                            + String.join("\n  - ", errors));
                }

                return new AlertConfig(this);
            }
        }
    }

    // ---------------------------------------------------------------
    // Service — now using Builder
    // ---------------------------------------------------------------
    static class AlertConfigurationService {

        /** Correct, readable, named — no positional guessing. */
        AlertConfig createCpuAlert() {
            return new AlertConfig.Builder()
                    .metricName("cpu_usage")
                    .threshold(80.0)
                    .channel("pagerduty")
                    .addRecipient("oncall@corp.com")
                    .priority(5)
                    .windowSeconds(300)
                    .maxRetries(3)
                    .build();
        }

        /** Optional fields get defaults — shorter, still valid. */
        AlertConfig createDiskAlert() {
            return new AlertConfig.Builder()
                    .metricName("disk_usage")
                    .threshold(95.0)
                    .channel("slack")
                    .addRecipient("storage@corp.com")
                    .addRecipient("sre@corp.com")
                    .priority(4)
                    .build(); // windowSeconds=300, maxRetries=3, isActive=true by default
        }

        /** Inactive staging alert — intent is explicit. */
        AlertConfig createStagingAlert() {
            return new AlertConfig.Builder()
                    .metricName("error_rate")
                    .threshold(5.0)
                    .channel("email")
                    .addRecipient("dev@corp.com")
                    .inactive()
                    .build();
        }

        /**
         * Demonstrates what happens with an invalid config.
         * build() throws IllegalStateException at construction time,
         * not silently at 2 AM in production.
         */
        void attemptInvalidAlert() {
            try {
                new AlertConfig.Builder()
                        .metricName("memory_usage")
                        // Missing: .threshold(...)     -> caught
                        // Missing: .channel(...)       -> caught
                        // Missing: .addRecipient(...)  -> caught
                        .build();
            } catch (IllegalStateException e) {
                System.out.println("Caught at build() time (good!):\n" + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        var service = new AlertConfigurationService();

        System.out.println("=== v3: Builder Pattern Applied ===\n");

        AlertConfig cpu = service.createCpuAlert();
        System.out.println("CPU alert:     " + cpu);

        AlertConfig disk = service.createDiskAlert();
        System.out.println("Disk alert:    " + disk);

        AlertConfig staging = service.createStagingAlert();
        System.out.println("Staging alert: " + staging);

        System.out.println();
        service.attemptInvalidAlert();

        System.out.println();
        System.out.println("Benefits over v1 and v2:");
        System.out.println("  1. Named steps — .threshold(80.0) not position 2");
        System.out.println("  2. Immutable — no mutation after creation");
        System.out.println("  3. Invariants enforced at build() — fail early, fail loudly");
        System.out.println("  4. Defaults documented in Builder — not scattered in callers");
        System.out.println("  5. Invalid objects cannot exist — compiler + build() cooperate");
    }
}
