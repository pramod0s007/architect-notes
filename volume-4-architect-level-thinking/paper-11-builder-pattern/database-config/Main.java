/**
 * Demonstrates the Builder pattern for database configuration.
 *
 * Three realistic scenarios:
 *   1. Minimal dev config  — only required fields
 *   2. Full production config — SSL, large pool, tuned timeouts
 *   3. Read-replica config  — primary + read replica for analytics
 */
public class Main {

    public static void main(String[] args) {

        // ── 1. Minimal dev config ─────────────────────────────────────────
        DatabaseConfig devConfig = new DatabaseConfig.Builder(
                "jdbc:postgresql://localhost:5432/myapp_dev",
                "dev_user",
                "dev_pass"
        ).build();

        System.out.println("=== Minimal Dev Config ===");
        System.out.println(devConfig);
        System.out.println("Pool size  : " + devConfig.getPoolSize());
        System.out.println("SSL        : " + devConfig.isSslEnabled());
        System.out.println("Connect tmo: " + devConfig.getConnectTimeoutMs() + " ms");

        // ── 2. Full production config ─────────────────────────────────────
        DatabaseConfig prodConfig = new DatabaseConfig.Builder(
                "jdbc:postgresql://prod-db.us-east-1.rds.amazonaws.com:5432/myapp",
                "app_user",
                System.getenv().getOrDefault("DB_PASSWORD", "secret")
        )
                .poolSize(50)
                .sslEnabled(true)
                .connectTimeoutMs(3_000L)
                .readTimeoutMs(15_000L)
                .maxRetries(5)
                .build();

        System.out.println("\n=== Full Production Config ===");
        System.out.println(prodConfig);
        System.out.println("Pool size  : " + prodConfig.getPoolSize());
        System.out.println("SSL        : " + prodConfig.isSslEnabled());
        System.out.println("Connect tmo: " + prodConfig.getConnectTimeoutMs() + " ms");
        System.out.println("Read tmo   : " + prodConfig.getReadTimeoutMs() + " ms");
        System.out.println("Max retries: " + prodConfig.getMaxRetries());

        // ── 3. Read-replica config ────────────────────────────────────────
        DatabaseConfig replicaConfig = new DatabaseConfig.Builder(
                "jdbc:postgresql://prod-db-primary.us-east-1.rds.amazonaws.com:5432/myapp",
                "app_user",
                System.getenv().getOrDefault("DB_PASSWORD", "secret")
        )
                .poolSize(20)
                .sslEnabled(true)
                .connectTimeoutMs(3_000L)
                .readTimeoutMs(20_000L)
                .readReplicaUrl("jdbc:postgresql://prod-db-replica.us-east-1.rds.amazonaws.com:5432/myapp")
                .build();

        System.out.println("\n=== Read-Replica Config ===");
        System.out.println(replicaConfig);
        System.out.println("Has replica: " + replicaConfig.hasReadReplica());
        System.out.println("Replica URL: " + replicaConfig.getReadReplicaUrl());

        // ── 4. Validation in action ───────────────────────────────────────
        System.out.println("\n=== Validation Demo ===");
        try {
            new DatabaseConfig.Builder(
                    "jdbc:postgresql://localhost:5432/test",
                    "user",
                    "pass"
            )
            .poolSize(200)   // too large — max is 100
            .build();
        } catch (IllegalStateException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        try {
            new DatabaseConfig.Builder("", "user", "pass").build();
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
    }
}
