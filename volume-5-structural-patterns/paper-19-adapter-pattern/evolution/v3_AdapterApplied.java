package evolution;

import java.util.List;
import java.util.Map;

/**
 * EVOLUTION v3 — Adapter Pattern Applied
 *
 * Domain: Database Connector (SQL vs NoSQL)
 *
 * MySqlAdapter implements DatabaseClient, wraps MySqlDriver.
 * MongoAdapter implements DatabaseClient, wraps MongoDriver.
 *
 * CustomerService is unchanged — still depends only on DatabaseClient.
 * MySqlDriver is unchanged — external library, untouched.
 * MongoDriver is unchanged — external library, untouched.
 *
 * ADAPTER RULE: Translate only. No business logic in adapters.
 * An adapter's only job is mapping one API to another.
 * If you find yourself adding validation, caching, or retry logic
 * to an adapter — extract those to a separate layer (Proxy, Decorator).
 */
public class v3_AdapterApplied {

    // ---------------------------------------------------------------
    // Target interface (same as v2)
    // ---------------------------------------------------------------
    interface DatabaseClient {
        void connect();
        List<Map<String, Object>> query(String sql);
        int update(String sql);
        void disconnect();
    }

    // ---------------------------------------------------------------
    // External drivers — UNCHANGED (third-party, not modified)
    // ---------------------------------------------------------------
    static class MySqlDriver {
        private final String connectionString;
        MySqlDriver(String conn) { this.connectionString = conn; }
        public void openConnection()  { System.out.println("  [MySqlDriver] openConnection: " + connectionString); }
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
        public void closeConnection() { System.out.println("  [MySqlDriver] closeConnection"); }
    }

    static class MongoDriver {
        private final String uri;
        MongoDriver(String uri) { this.uri = uri; }
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
    // Adapters — TRANSLATE ONLY. No business logic.
    // ---------------------------------------------------------------

    /**
     * MySqlAdapter: maps DatabaseClient API -> MySqlDriver API.
     * Rule: translate only — no caching, no retry, no validation here.
     */
    static class MySqlAdapter implements DatabaseClient {
        private final MySqlDriver driver;

        MySqlAdapter(String connectionString) {
            this.driver = new MySqlDriver(connectionString);
        }

        // Translate: connect() -> openConnection()
        @Override public void connect()     { driver.openConnection(); }

        // Translate: query(sql) -> executeQuery(sql) [same signature, different name]
        @Override public List<Map<String, Object>> query(String sql) {
            return driver.executeQuery(sql);
        }

        // Translate: update(sql) -> executeUpdate(sql) [same signature, different name]
        @Override public int update(String sql) { return driver.executeUpdate(sql); }

        // Translate: disconnect() -> closeConnection()
        @Override public void disconnect()  { driver.closeConnection(); }
    }

    /**
     * MongoAdapter: maps DatabaseClient API -> MongoDriver API.
     * Rule: translate only.
     *
     * The SQL-style query string is parsed minimally to extract
     * the collection name and construct a Mongo-style filter.
     * This is TRANSLATION, not business logic.
     */
    static class MongoAdapter implements DatabaseClient {
        private final MongoDriver driver;
        private final String      defaultCollection;

        MongoAdapter(String uri, String defaultCollection) {
            this.driver            = new MongoDriver(uri);
            this.defaultCollection = defaultCollection;
        }

        // Translate: connect() -> init()
        @Override public void connect()    { driver.init(); }

        // Translate: query(sql) -> find(collection, filter)
        // Minimal SQL parsing — extraction of intent, not a real SQL parser
        @Override public List<Map<String, Object>> query(String sql) {
            // Extract collection from "SELECT ... FROM <collection> [WHERE ...]"
            String collection = extractCollection(sql);
            Map<String, Object> filter = extractFilter(sql);
            return driver.find(collection, filter);
        }

        // Translate: update(sql) -> insertOne(collection, document)
        @Override public int update(String sql) {
            String collection = extractCollection(sql);
            Map<String, Object> doc = extractInsertValues(sql);
            boolean ok = driver.insertOne(collection, doc);
            return ok ? 1 : 0;
        }

        // Translate: disconnect() -> close()
        @Override public void disconnect() { driver.close(); }

        // --- Translation helpers (pure string parsing — not business logic) ---
        private String extractCollection(String sql) {
            String lower = sql.toLowerCase();
            if (lower.contains("from"))    return parseAfterKeyword(sql, "FROM");
            if (lower.contains("into"))    return parseAfterKeyword(sql, "INTO");
            return defaultCollection;
        }

        private String parseAfterKeyword(String sql, String keyword) {
            int idx = sql.toUpperCase().indexOf(keyword);
            if (idx < 0) return defaultCollection;
            String after = sql.substring(idx + keyword.length()).trim();
            String[] tokens = after.split("\\s+");
            return tokens.length > 0 ? tokens[0].toLowerCase() : defaultCollection;
        }

        private Map<String, Object> extractFilter(String sql) {
            // Simplified: return empty filter (find all) if no WHERE clause
            if (!sql.toUpperCase().contains("WHERE")) return Map.of();
            // In real adapter: parse WHERE clause into Mongo filter document
            return Map.of("_filter", sql.substring(sql.toUpperCase().indexOf("WHERE") + 5).trim());
        }

        private Map<String, Object> extractInsertValues(String sql) {
            // Simplified: extract VALUES portion for translation
            return Map.of("_values", sql, "_from", "adapter");
        }
    }

    // ---------------------------------------------------------------
    // CustomerService — UNCHANGED from v2
    // ---------------------------------------------------------------
    static class CustomerService {
        private final DatabaseClient db;

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
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v3: Adapter Pattern Applied ===\n");

        System.out.println("--- CustomerService with MySQL (via MySqlAdapter) ---");
        CustomerService mysqlService = new CustomerService(
                new MySqlAdapter("jdbc:mysql://localhost:3306/customers"));
        mysqlService.findAllCustomers().forEach(c -> System.out.println("  " + c));
        mysqlService.createCustomer("Alice", "alice@corp.com");
        mysqlService.close();

        System.out.println();
        System.out.println("--- CustomerService with MongoDB (via MongoAdapter) ---");
        CustomerService mongoService = new CustomerService(
                new MongoAdapter("mongodb://localhost:27017", "customers"));
        mongoService.findAllCustomers().forEach(c -> System.out.println("  " + c));
        mongoService.createCustomer("Charlie", "charlie@corp.com");
        mongoService.close();

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println("CustomerService: unchanged.");
        System.out.println("MySqlDriver:     unchanged.");
        System.out.println("MongoDriver:     unchanged.");
        System.out.println("MySqlAdapter:    translates DatabaseClient -> MySqlDriver. Nothing else.");
        System.out.println("MongoAdapter:    translates DatabaseClient -> MongoDriver. Nothing else.");
        System.out.println();
        System.out.println("Adapter Rule: translate only.");
        System.out.println("If you add retry, caching, or validation to an adapter:");
        System.out.println("  -> retry    belongs in a Proxy or Resilience wrapper");
        System.out.println("  -> caching  belongs in a CachingProxy (see Paper 18 v3)");
        System.out.println("  -> validation belongs in the service or a Guard");
        System.out.println("Adapters that grow fat become maintenance problems.");
    }
}
