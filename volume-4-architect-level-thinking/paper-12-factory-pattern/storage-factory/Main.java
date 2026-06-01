import java.nio.charset.StandardCharsets;

/**
 * Demonstrates the Factory pattern for storage providers.
 *
 * Application code works exclusively with the {@link StorageProvider}
 * interface; the factory decides which concrete class to instantiate.
 */
public class Main {

    public static void main(String[] args) {

        byte[] payload = "Hello, Storage World!".getBytes(StandardCharsets.UTF_8);

        // ── 1. S3 Provider ────────────────────────────────────────────────
        System.out.println("=== S3 Storage Provider ===");
        StorageProvider s3 = StorageFactory.create("s3");
        exerciseProvider(s3, "uploads/2024/report.txt", payload);

        // ── 2. Azure Blob Provider ────────────────────────────────────────
        System.out.println("\n=== Azure Blob Storage Provider ===");
        StorageProvider azure = StorageFactory.create("azure");
        exerciseProvider(azure, "assets/images/logo.png", payload);

        // ── 3. Local Disk Provider ────────────────────────────────────────
        System.out.println("\n=== Local Disk Storage Provider ===");
        StorageProvider local = StorageFactory.create("local");
        exerciseProvider(local, "temp/test-file.txt", payload);

        // ── 4. From environment / system property ─────────────────────────
        System.out.println("\n=== From Environment (STORAGE_PROVIDER system property) ===");
        StorageProvider env = StorageFactory.createFromEnvironment();
        System.out.println("Provider type: " + env.getClass().getSimpleName());
        env.upload("env-test/data.bin", payload);

        // ── 5. Unknown provider — error handling ──────────────────────────
        System.out.println("\n=== Unknown Provider Error ===");
        try {
            StorageFactory.create("gcs");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }
    }

    /**
     * Upload, download, verify content, then delete — exercises all three
     * interface methods with the given provider transparently.
     */
    private static void exerciseProvider(StorageProvider provider, String key, byte[] data) {
        provider.upload(key, data);

        byte[] downloaded = provider.download(key);
        if (downloaded != null) {
            String content = new String(downloaded, StandardCharsets.UTF_8);
            System.out.println("  Verified content: \"" + content + "\"");
        }

        provider.delete(key);
        provider.download(key);  // confirm deletion
    }
}
