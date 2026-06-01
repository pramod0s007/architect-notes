package evolution;

/**
 * EVOLUTION v1 — Scattered `new` Problem
 *
 * Domain: Logger Factory
 *
 * Problem: Each service creates its own logger directly.
 * When the team switches to CloudLogger for production, someone
 * must hunt down every `new ConsoleLogger()` and `new FileLogger()`.
 * Three of four services are updated. One is missed. Production
 * still writes to local disk. Incident data is lost.
 *
 * The missed service is OrderService — it logs to a file that
 * doesn't exist in the production container.
 */
public class v1_ScatteredNew {

    // ---------------------------------------------------------------
    // Logger implementations — each has different construction cost
    // ---------------------------------------------------------------
    static class ConsoleLogger {
        public void log(String level, String message) {
            System.out.println("[CONSOLE][" + level + "] " + message);
        }
    }

    static class FileLogger {
        private final String filePath;

        public FileLogger(String filePath) {
            this.filePath = filePath;
            System.out.println("  (FileLogger opened: " + filePath + ")");
        }

        public void log(String level, String message) {
            // In real life: write to file. Here: simulate.
            System.out.println("[FILE:" + filePath + "][" + level + "] " + message);
        }
    }

    static class CloudLogger {
        private final String endpoint;

        public CloudLogger(String endpoint) {
            this.endpoint = endpoint;
            System.out.println("  (CloudLogger connected: " + endpoint + ")");
        }

        public void log(String level, String message) {
            System.out.println("[CLOUD:" + endpoint + "][" + level + "] " + message);
        }
    }

    // ---------------------------------------------------------------
    // Service 1 — UserService: updated to CloudLogger for prod
    // ---------------------------------------------------------------
    static class UserService {
        // UPDATED during production migration
        private final CloudLogger logger = new CloudLogger("https://logs.corp.com/users");

        public void createUser(String name) {
            logger.log("INFO", "Creating user: " + name);
        }
    }

    // ---------------------------------------------------------------
    // Service 2 — PaymentService: updated to CloudLogger for prod
    // ---------------------------------------------------------------
    static class PaymentService {
        // UPDATED during production migration
        private final CloudLogger logger = new CloudLogger("https://logs.corp.com/payments");

        public void processPayment(double amount) {
            logger.log("INFO", "Processing payment: $" + amount);
        }
    }

    // ---------------------------------------------------------------
    // Service 3 — InventoryService: updated to CloudLogger for prod
    // ---------------------------------------------------------------
    static class InventoryService {
        // UPDATED during production migration
        private final CloudLogger logger = new CloudLogger("https://logs.corp.com/inventory");

        public void updateStock(String sku, int quantity) {
            logger.log("INFO", "Stock update: " + sku + " -> " + quantity);
        }
    }

    // ---------------------------------------------------------------
    // Service 4 — OrderService: MISSED during migration
    // ---------------------------------------------------------------
    static class OrderService {
        // BUG: Still using FileLogger from before the migration.
        // The file /var/log/orders.log does not exist in the production container.
        // Orders are placed, logs are silently dropped. No error, no alert.
        private final FileLogger logger = new FileLogger("/var/log/orders.log");

        public void placeOrder(String orderId) {
            logger.log("INFO", "Order placed: " + orderId);
            // In production: this log message never reaches the cloud.
            // The incident-response team cannot reconstruct what happened.
        }
    }

    // ---------------------------------------------------------------
    // Main — simulate the production state
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v1: Scattered `new` — Production State ===\n");

        System.out.println("--- UserService ---");
        new UserService().createUser("alice");

        System.out.println("\n--- PaymentService ---");
        new PaymentService().processPayment(199.99);

        System.out.println("\n--- InventoryService ---");
        new InventoryService().updateStock("SKU-001", 50);

        System.out.println("\n--- OrderService (MISSED) ---");
        new OrderService().placeOrder("ORD-8821");

        System.out.println("\n=== Problem Summary ===");
        System.out.println("3 of 4 services log to Cloud. 1 still logs to disk.");
        System.out.println("The logger type is a detail that each service owns.");
        System.out.println("Migration requires searching every file. One was missed.");
        System.out.println("Fix: move logger creation to ONE place — Factory Pattern.");
    }
}
