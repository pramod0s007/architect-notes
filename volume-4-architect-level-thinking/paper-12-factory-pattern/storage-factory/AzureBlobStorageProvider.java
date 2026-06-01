import java.util.HashMap;
import java.util.Map;

/**
 * Simulated Azure Blob Storage provider.
 *
 * In production this would use the Azure Storage Blobs SDK
 * (BlobServiceClient / BlobContainerClient). Here we use an in-memory map
 * so the example is runnable without an Azure subscription.
 */
public class AzureBlobStorageProvider implements StorageProvider {

    private final String accountName;
    private final String containerName;
    private final Map<String, byte[]> store = new HashMap<>();

    public AzureBlobStorageProvider(String accountName, String containerName) {
        this.accountName   = accountName;
        this.containerName = containerName;
        System.out.println("[Azure] Initialized — account: " + accountName
                + ", container: " + containerName);
    }

    @Override
    public void upload(String key, byte[] data) {
        store.put(key, data);
        System.out.println("[Azure] UPLOAD https://" + accountName
                + ".blob.core.windows.net/" + containerName + "/" + key
                + " (" + data.length + " bytes)");
    }

    @Override
    public byte[] download(String key) {
        byte[] data = store.get(key);
        if (data == null) {
            System.out.println("[Azure] DOWNLOAD " + key + " -> BlobNotFound");
            return null;
        }
        System.out.println("[Azure] DOWNLOAD " + key + " (" + data.length + " bytes)");
        return data;
    }

    @Override
    public void delete(String key) {
        boolean existed = store.remove(key) != null;
        System.out.println("[Azure] DELETE " + key
                + (existed ? " -> deleted" : " -> not found, no-op"));
    }
}
