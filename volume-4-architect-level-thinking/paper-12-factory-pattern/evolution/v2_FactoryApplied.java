package evolution;

/**
 * EVOLUTION v2 — Factory Pattern Applied
 *
 * Domain: Logger Factory
 *
 * Improvement over v1: Services no longer call `new` on a logger.
 * LoggerFactory.create(environment) decides which implementation to return.
 * Switching from FileLogger to CloudLogger in production = one change,
 * in one class, tested once.
 *
 * Still limited: adding a new logger type (e.g., SplunkLogger) requires
 * modifying LoggerFactory — it violates the Open/Closed Principle.
 * See v3 for the Registry approach.
 */
public class v2_FactoryApplied {

    // ---------------------------------------------------------------
    // Logger interface — services depend on this abstraction only
    // ---------------------------------------------------------------
    interface Logger {
        void log(String level, String message);
    }

    // ---------------------------------------------------------------
    // Implementations — services never reference these directly
    // ---------------------------------------------------------------
    static class ConsoleLogger implements Logger {
        @Override
        public void log(String level, String message) {
            System.out.println("[CONSOLE][" + level + "] " + message);
        }
    }

    static class FileLogger implements Logger {
        private final String filePath;

        FileLogger(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void log(String level, String message) {
            System.out.println("[FILE:" + filePath + "][" + level + "] " + message);
        }
    }

    static class CloudLogger implements Logger {
        private final String endpoint;

        CloudLogger(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void log(String level, String message) {
            System.out.println("[CLOUD:" + endpoint + "][" + level + "] " + message);
        }
    }

    // ---------------------------------------------------------------
    // Factory — ONE place that decides which logger to create
    // ---------------------------------------------------------------
    static class LoggerFactory {

        /**
         * Returns the right logger for the current environment.
         *
         * Migration from FileLogger to CloudLogger = change this method once.
         * All services automatically pick up the new implementation.
         *
         * Limitation (to be fixed in v3): adding SplunkLogger requires
         * adding another `case` here — open/closed violated.
         */
        public static Logger create(String environment, String serviceName) {
            return switch (environment.toLowerCase()) {
                case "local", "test" ->
                        new ConsoleLogger();
                case "staging" ->
                        new FileLogger("/var/log/" + serviceName + ".log");
                case "production" ->
                        new CloudLogger("https://logs.corp.com/" + serviceName);
                default ->
                        throw new IllegalArgumentException(
                                "Unknown environment: " + environment);
            };
        }
    }

    // ---------------------------------------------------------------
    // Services — identical structure, none does `new ConcreteLogger()`
    // ---------------------------------------------------------------
    static class UserService {
        private final Logger logger;

        UserService(String environment) {
            this.logger = LoggerFactory.create(environment, "users");
        }

        public void createUser(String name) {
            logger.log("INFO", "Creating user: " + name);
        }
    }

    static class PaymentService {
        private final Logger logger;

        PaymentService(String environment) {
            this.logger = LoggerFactory.create(environment, "payments");
        }

        public void processPayment(double amount) {
            logger.log("INFO", "Processing payment: $" + amount);
        }
    }

    static class InventoryService {
        private final Logger logger;

        InventoryService(String environment) {
            this.logger = LoggerFactory.create(environment, "inventory");
        }

        public void updateStock(String sku, int quantity) {
            logger.log("INFO", "Stock update: " + sku + " -> " + quantity);
        }
    }

    static class OrderService {
        private final Logger logger;

        OrderService(String environment) {
            this.logger = LoggerFactory.create(environment, "orders");
        }

        public void placeOrder(String orderId) {
            logger.log("INFO", "Order placed: " + orderId);
        }
    }

    // ---------------------------------------------------------------
    // Main — show the same 4 services in local vs production
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v2: Factory Pattern Applied ===\n");

        String env = "production"; // Switch this one string to change ALL loggers

        System.out.println("Environment: " + env + "\n");

        new UserService(env).createUser("alice");
        new PaymentService(env).processPayment(199.99);
        new InventoryService(env).updateStock("SKU-001", 50);
        new OrderService(env).placeOrder("ORD-8821");

        System.out.println();
        System.out.println("--- Now run locally ---");
        String localEnv = "local";
        new UserService(localEnv).createUser("bob");
        new OrderService(localEnv).placeOrder("ORD-0001");

        System.out.println();
        System.out.println("=== Improvement over v1 ===");
        System.out.println("One env variable controls all 4 services.");
        System.out.println("No service knows whether it is Console, File, or Cloud.");
        System.out.println("Migration: change factory once. Zero service changes.");
        System.out.println();
        System.out.println("=== Remaining limitation ===");
        System.out.println("Adding SplunkLogger requires editing LoggerFactory.create().");
        System.out.println("That means touching a shared class every time a new logger appears.");
        System.out.println("See v3: Registry Pattern removes this last coupling.");
    }
}
