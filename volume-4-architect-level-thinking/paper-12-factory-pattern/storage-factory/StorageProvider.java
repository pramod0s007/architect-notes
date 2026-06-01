/**
 * Unified interface for blob/object storage operations.
 *
 * All production implementations (S3, Azure Blob, Local disk) conform to
 * this contract, so application code never depends on a concrete provider.
 */
public interface StorageProvider {

    /**
     * Upload data at the given key (path/filename).
     *
     * @param key  unique identifier within the storage (e.g. "uploads/photo.jpg")
     * @param data raw bytes to store
     */
    void upload(String key, byte[] data);

    /**
     * Download and return data stored at {@code key}.
     *
     * @param key unique identifier
     * @return raw bytes, or {@code null} if not found
     */
    byte[] download(String key);

    /**
     * Delete the object at {@code key}. No-op if the key does not exist.
     *
     * @param key unique identifier
     */
    void delete(String key);
}
