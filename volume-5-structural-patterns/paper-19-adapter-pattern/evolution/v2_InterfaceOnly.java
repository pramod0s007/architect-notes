package evolution;

import java.util.List;
import java.util.Map;

/**
 * EVOLUTION v2 — Interface Extracted, But Drivers Can't Implement It
 *
 * Domain: Database Connector (SQL vs NoSQL)
 *
 * An interface (DatabaseClient) is extracted so CustomerService depends
 * on an abstraction, not a concrete class. This is the right direction.
 *
 * But there is a problem:
 * MySqlDriver is a third-party library — its source cannot be modified.
 * It cannot be made to implement DatabaseClient.
 * MongoDriver is also external — same limitation.
 *
 * Attempting to use MySqlDriver directly still requires type-casting or
 * wrapping it. The interface alone is not enough.
 *
 * This is the pressure that motivates the Adapter Pattern in v3:
 * "I have the right interface. I have the right drivers.
 *  But they don't speak the same language."
 */
public class v2_InterfaceOnly {

    // ---------------------------------------------------------------
    // Target interface — what CustomerService needs
    // ---------------------------------------------------------------
    interface DatabaseClient {
        void connect();
        List<Map<String, Object>> query(String sql);
        int update(String sql);
        void disconnect();
    }

    // ---------------------------------------------------------------
    // External drivers — cannot implement DatabaseClient (third-party)
    // ---------------------------------------------------------------

    /** External library: MySqlDriver. We cannot add `implements DatabaseClient`. */
    static class MySqlDriver {
        private final String connectionString;
        private boolean connected = false;

        MySqlDriver(String conn) { this.connectionString = conn; }

        // Method names differ from DatabaseClient — different API contract
        public void openConnection()  {
            connected = true;
            System.out.println("  [MySqlDriver] openConnection: " + connectionString);
        }
        public List<Map<String, Object>> executeQuery(String sql) {
            System.out.println("  [MySqlDriver] executeQuery: " + sql);
            return List.of(
                    Map.of("id", 1L, "name", "Alice", "email", "alice@corp.com"),
                    Map.of("id", 2L, "name", "Bob",   "email", "bob@corp.com"));
        }
        public int executeUpdate(String sql) {
            System.out.println("  [MySqlDriver] executeUpdate: " + sql);
            return 1;
        }
        public void closeConnection() {
            connected = false;
            System.out.println("  [MySqlDriver] closeConnection");
        }
    }

    /** External library: MongoDriver. Different API — cannot implement DatabaseClient. */
    static class MongoDriver {
        private final String uri;

        MongoDriver(String uri) { this.uri = uri; }

        // Completely different method names and signatures
        public void init()  { System.out.println("  [MongoDriver] init: " + uri); }
        public List<Map<String, Object>> find(String collection, Map<String, Object> filter) {
            System.out.println("  [MongoDriver] find: collection=" + collection + " filter=" + filter);
            return List.of(Map.of("_id", "abc", "name", "Charlie", "email", "charlie@corp.com"));
        }
        public boolean insertOne(String collection, Map<String, Object> document) {
            System.out.println("  [MongoDriver] insertOne: " + collection + " -> " + document);
            return true;
        }
        public void close() { System.out.println("  [MongoDriver] close"); }
    }

    // ---------------------------------------------------------------
    // CustomerService — depends on DatabaseClient, not on drivers
    // ---------------------------------------------------------------
    static class CustomerService {
        private final DatabaseClient db;

        // Now depends on the interface — this is correct
        CustomerService(DatabaseClient db) {
            this.db = db;
            this.db.connect();
        }

        public List<Map<String, Object>> findAllCustomers() {
            return db.query("SELECT id, name, email FROM customers");
        }

        public Map<String, Object> findCustomerById(long id) {
            List<Map<String, Object>> results = db.query(
                    "SELECT id, name, email FROM customers WHERE id = " + id);
            if (results.isEmpty()) throw new RuntimeException("Customer not found: " + id);
            return results.get(0);
        }

        public void createCustomer(String name, String email) {
            int rows = db.update(
                    "INSERT INTO customers (name, email) VALUES ('" + name + "', '" + email + "')");
            System.out.println("  Created. Rows affected: " + rows);
        }

        public void close() { db.disconnect(); }
    }

    // ---------------------------------------------------------------
    // The problem — neither driver implements DatabaseClient
    // ---------------------------------------------------------------

    /**
     * PROBLEM: We can't do this:
     *   CustomerService svc = new CustomerService(new MySqlDriver("..."));
     * because MySqlDriver does not implement DatabaseClient.
     *
     * We can't modify MySqlDriver — it's a third-party jar.
     * We need something to BRIDGE the gap.
     * That is what the Adapter Pattern does in v3.
     */

    // ---------------------------------------------------------------
    // Main — demonstrate the gap
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v2: Interface Extracted — Gap Remains ===\n");

        System.out.println("CustomerService needs a DatabaseClient.");
        System.out.println("MySqlDriver and MongoDriver exist but don't implement DatabaseClient.");
        System.out.println("We can't modify them — they're external libraries.");
        System.out.println();
        System.out.println("The gap:");
        System.out.println("  DatabaseClient.connect()      <-> MySqlDriver.openConnection()");
        System.out.println("  DatabaseClient.query(sql)     <-> MySqlDriver.executeQuery(sql)");
        System.out.println("  DatabaseClient.update(sql)    <-> MySqlDriver.executeUpdate(sql)");
        System.out.println("  DatabaseClient.disconnect()   <-> MySqlDriver.closeConnection()");
        System.out.println();
        System.out.println("  DatabaseClient.connect()      <-> MongoDriver.init()");
        System.out.println("  DatabaseClient.query(sql)     <-> MongoDriver.find(collection, filter)");
        System.out.println("  DatabaseClient.update(sql)    <-> MongoDriver.insertOne(collection, doc)");
        System.out.println("  DatabaseClient.disconnect()   <-> MongoDriver.close()");
        System.out.println();
        System.out.println("The adapter translates CustomerService's language into each driver's.");
        System.out.println("See v3 for MySqlAdapter and MongoAdapter.");
    }
}
