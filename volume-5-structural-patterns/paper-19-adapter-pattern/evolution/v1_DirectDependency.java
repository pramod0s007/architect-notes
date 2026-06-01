package evolution;

import java.util.List;
import java.util.Map;

/**
 * EVOLUTION v1 — Direct Dependency on MySqlDriver
 *
 * Domain: Database Connector (SQL vs NoSQL)
 *
 * CustomerService directly imports and uses MySqlDriver.
 * Every method calls executeQuery(), getResults(), executeUpdate() — all
 * methods specific to MySqlDriver's API.
 *
 * Problem: CustomerService is coupled to MySQL forever.
 * If the team migrates to PostgreSQL, or adds a MongoDB service for
 * a new region, CustomerService must be rewritten from scratch.
 * The driver-specific method names, result types, and error types
 * are embedded throughout the service.
 */
public class v1_DirectDependency {

    // ---------------------------------------------------------------
    // MySqlDriver — simulates an external JDBC-like library
    // Cannot be modified (third-party jar).
    // ---------------------------------------------------------------
    static class MySqlDriver {
        private final String connectionString;
        private boolean connected = false;

        MySqlDriver(String connectionString) {
            this.connectionString = connectionString;
        }

        /** MySQL-specific connect method */
        public void openConnection() {
            this.connected = true;
            System.out.println("  [MySqlDriver] Connected to: " + connectionString);
        }

        /** MySQL-specific query execution */
        public List<Map<String, Object>> executeQuery(String sql) {
            if (!connected) throw new IllegalStateException("MySqlDriver: not connected");
            System.out.println("  [MySqlDriver] executeQuery: " + sql);
            // Simulated result
            return List.of(
                    Map.of("id", 1L, "name", "Alice", "email", "alice@corp.com"),
                    Map.of("id", 2L, "name", "Bob",   "email", "bob@corp.com")
            );
        }

        /** MySQL-specific write */
        public int executeUpdate(String sql) {
            if (!connected) throw new IllegalStateException("MySqlDriver: not connected");
            System.out.println("  [MySqlDriver] executeUpdate: " + sql);
            return 1; // rows affected
        }

        /** MySQL-specific close */
        public void closeConnection() {
            this.connected = false;
            System.out.println("  [MySqlDriver] Connection closed");
        }
    }

    // ---------------------------------------------------------------
    // CustomerService — directly coupled to MySqlDriver
    // ---------------------------------------------------------------
    static class CustomerService {
        // DIRECT DEPENDENCY: field type is MySqlDriver, not an interface
        private final MySqlDriver driver;

        CustomerService(String connectionString) {
            // DIRECT CONSTRUCTION: service owns the driver lifecycle
            this.driver = new MySqlDriver(connectionString);
            this.driver.openConnection();
        }

        /** MySQL-specific method calls throughout */
        public List<Map<String, Object>> findAllCustomers() {
            // Calling MySQL-specific API method: executeQuery
            return driver.executeQuery("SELECT id, name, email FROM customers");
        }

        public Map<String, Object> findCustomerById(long id) {
            // String-concatenated SQL — MySQL dialect
            List<Map<String, Object>> results = driver.executeQuery(
                    "SELECT id, name, email FROM customers WHERE id = " + id);
            if (results.isEmpty()) throw new RuntimeException("Customer not found: " + id);
            return results.get(0);
        }

        public void createCustomer(String name, String email) {
            // MySQL-specific API: executeUpdate
            int rows = driver.executeUpdate(
                    "INSERT INTO customers (name, email) VALUES ('" + name + "', '" + email + "')");
            System.out.println("  Created customer. Rows affected: " + rows);
        }

        public void close() {
            // Calling MySQL-specific close method
            driver.closeConnection();
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v1: Direct Dependency on MySqlDriver ===\n");

        CustomerService service = new CustomerService("jdbc:mysql://localhost:3306/customers");

        System.out.println("--- findAllCustomers ---");
        List<Map<String, Object>> customers = service.findAllCustomers();
        customers.forEach(c -> System.out.println("  " + c));

        System.out.println("\n--- findCustomerById ---");
        Map<String, Object> customer = service.findCustomerById(1L);
        System.out.println("  " + customer);

        System.out.println("\n--- createCustomer ---");
        service.createCustomer("Carol", "carol@corp.com");

        service.close();

        System.out.println();
        System.out.println("Problem: CustomerService uses executeQuery(), executeUpdate(),");
        System.out.println("openConnection(), closeConnection() — all MySqlDriver-specific.");
        System.out.println("Migrating to MongoDB or PostgreSQL = rewrite CustomerService.");
        System.out.println("v2 extracts an interface. v3 adapts external drivers to that interface.");
    }
}
