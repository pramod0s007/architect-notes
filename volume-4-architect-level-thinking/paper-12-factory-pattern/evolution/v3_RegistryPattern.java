package evolution;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * EVOLUTION v3 — Registry Pattern (Dynamic Logger Registration)
 *
 * Domain: Logger Factory
 *
 * Improvement over v2: LoggerFactory no longer has a switch statement.
 * New logger types are registered at startup — zero changes to the registry,
 * zero changes to services.
 *
 * Adding SplunkLogger for compliance team:
 *   registry.register("splunk", () -> new SplunkLogger(config));
 * That is the entire change. No other file is touched.
 */
public class v3_RegistryPattern {

    // ---------------------------------------------------------------
    // Logger interface
    // ---------------------------------------------------------------
    interface Logger {
        void log(String level, String message);
    }

    // ---------------------------------------------------------------
    // Implementations — each is a standalone class
    // ---------------------------------------------------------------
    static class ConsoleLogger implements Logger {
        @Override
        public void log(String level, String message) {
            System.out.println("[CONSOLE][" + level + "] " + message);
        }
    }

    static class FileLogger implements Logger {
        private final String path;
        FileLogger(String path) { this.path = path; }

        @Override
        public void log(String level, String message) {
            System.out.println("[FILE:" + path + "][" + level + "] " + message);
        }
    }

    static class CloudLogger implements Logger {
        private final String endpoint;
        CloudLogger(String endpoint) { this.endpoint = endpoint; }

        @Override
        public void log(String level, String message) {
            System.out.println("[CLOUD:" + endpoint + "][" + level + "] " + message);
        }
    }

    /** Added by the compliance team — zero changes to LoggerRegistry. */
    static class SplunkLogger implements Logger {
        private final String indexName;
        SplunkLogger(String indexName) { this.indexName = indexName; }

        @Override
        public void log(String level, String message) {
            System.out.println("[SPLUNK:index=" + indexName + "][" + level + "] " + message);
        }
    }

    /** Added for async streaming — also zero changes to LoggerRegistry. */
    static class KafkaLogger implements Logger {
        private final String topic;
        KafkaLogger(String topic) { this.topic = topic; }

        @Override
        public void log(String level, String message) {
            System.out.println("[KAFKA:topic=" + topic + "][" + level + "] " + message);
        }
    }

    // ---------------------------------------------------------------
    // LoggerRegistry — no switch, no if-else, open for extension
    // ---------------------------------------------------------------
    static class LoggerRegistry {
        private final Map<String, Supplier<Logger>> registry = new HashMap<>();

        /**
         * Register a named logger factory.
         * The Supplier is called each time create() is invoked for this key,
         * so each service gets its own fresh instance.
         */
        public LoggerRegistry register(String name, Supplier<Logger> factory) {
            registry.put(name.toLowerCase(), factory);
            return this; // fluent chaining
        }

        /**
         * Create a logger by name.
         * Throws if the name was not registered — fail fast, fail loud.
         */
        public Logger create(String name) {
            Supplier<Logger> factory = registry.get(name.toLowerCase());
            if (factory == null) {
                throw new IllegalArgumentException(
                        "No logger registered for '" + name + "'. "
                        + "Registered: " + registry.keySet());
            }
            return factory.get();
        }

        public boolean isRegistered(String name) {
            return registry.containsKey(name.toLowerCase());
        }
    }

    // ---------------------------------------------------------------
    // Services — depend on Logger interface only
    // ---------------------------------------------------------------
    static class UserService {
        private final Logger logger;
        UserService(Logger logger) { this.logger = logger; }
        public void createUser(String name) { logger.log("INFO", "Creating user: " + name); }
    }

    static class PaymentService {
        private final Logger logger;
        PaymentService(Logger logger) { this.logger = logger; }
        public void processPayment(double amount) { logger.log("INFO", "Processing payment: $" + amount); }
    }

    static class InventoryService {
        private final Logger logger;
        InventoryService(Logger logger) { this.logger = logger; }
        public void updateStock(String sku, int qty) { logger.log("INFO", "Stock: " + sku + " -> " + qty); }
    }

    static class OrderService {
        private final Logger logger;
        OrderService(Logger logger) { this.logger = logger; }
        public void placeOrder(String id) { logger.log("INFO", "Order placed: " + id); }
    }

    // ---------------------------------------------------------------
    // Bootstrap — the only place that knows about concrete implementations
    // ---------------------------------------------------------------
    static LoggerRegistry buildRegistry(String environment) {
        LoggerRegistry registry = new LoggerRegistry()
                .register("console", ConsoleLogger::new)
                .register("file",    () -> new FileLogger("/var/log/app.log"))
                .register("cloud",   () -> new CloudLogger("https://logs.corp.com"))
                .register("splunk",  () -> new SplunkLogger("prod-logs"))   // compliance
                .register("kafka",   () -> new KafkaLogger("app-logs"));    // analytics

        return registry;
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v3: Registry Pattern ===\n");

        LoggerRegistry registry = buildRegistry("production");

        // Services receive a Logger — they don't know which type
        UserService      users     = new UserService(registry.create("cloud"));
        PaymentService   payments  = new PaymentService(registry.create("cloud"));
        InventoryService inventory = new InventoryService(registry.create("kafka"));
        OrderService     orders    = new OrderService(registry.create("splunk"));

        System.out.println("--- Normal operations ---");
        users.createUser("alice");
        payments.processPayment(199.99);
        inventory.updateStock("SKU-001", 50);
        orders.placeOrder("ORD-8821");

        System.out.println();
        System.out.println("--- Compliance team adds SplunkLogger ---");
        System.out.println("  (already registered above — zero other changes)");
        Logger complianceLogger = registry.create("splunk");
        complianceLogger.log("AUDIT", "Payment audit trail started");

        System.out.println();
        System.out.println("--- Unknown logger name ---");
        try {
            registry.create("datadog");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Comparison ===");
        System.out.println("v1: 4 files change per migration.");
        System.out.println("v2: 1 file changes (factory), but still has switch statement.");
        System.out.println("v3: 0 service files change. New logger = 1 registry.register() call.");
    }
}
