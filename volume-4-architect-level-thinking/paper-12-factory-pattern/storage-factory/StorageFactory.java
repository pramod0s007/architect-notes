/**
 * Factory that creates the appropriate {@link StorageProvider} based on a
 * provider name string or the {@code STORAGE_PROVIDER} system property.
 *
 * Centralises the mapping from environment/config to concrete implementation,
 * so application code never imports a provider-specific class.
 */
public class StorageFactory {

    private static final String DEFAULT_S3_BUCKET        = "my-app-assets";
    private static final String DEFAULT_AZURE_ACCOUNT    = "myappstorageaccount";
    private static final String DEFAULT_AZURE_CONTAINER  = "assets";
    private static final String DEFAULT_LOCAL_BASE_PATH  = "/tmp/local-storage";

    /**
     * Create a provider by name.
     *
     * @param provider one of {@code "s3"}, {@code "azure"}, {@code "local"} (case-insensitive)
     * @return a ready-to-use {@link StorageProvider}
     * @throws IllegalArgumentException for unknown provider names
     */
    public static StorageProvider create(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider name cannot be null");
        }
        switch (provider.trim().toLowerCase()) {
            case "s3":
                return new S3StorageProvider(DEFAULT_S3_BUCKET);
            case "azure":
                return new AzureBlobStorageProvider(DEFAULT_AZURE_ACCOUNT, DEFAULT_AZURE_CONTAINER);
            case "local":
                return new LocalDiskStorageProvider(DEFAULT_LOCAL_BASE_PATH);
            default:
                throw new IllegalArgumentException(
                    "Unknown storage provider: '" + provider + "'. " +
                    "Supported values: s3, azure, local");
        }
    }

    /**
     * Create a provider from the {@code STORAGE_PROVIDER} system property,
     * defaulting to {@code "local"} if the property is not set.
     *
     * Usage: {@code java -DSTORAGE_PROVIDER=s3 ...}
     */
    public static StorageProvider createFromEnvironment() {
        String provider = System.getProperty("STORAGE_PROVIDER", "local");
        System.out.println("[StorageFactory] STORAGE_PROVIDER=" + provider);
        return create(provider);
    }

    private StorageFactory() { /* utility class */ }
}
