package adapter.storageadapter;

import java.util.HashMap;
import java.util.Map;

// Simulates Azure Blob Storage SDK — different incompatible interface
public class AzureBlobClient {

    private final Map<String, byte[]> container = new HashMap<>();
    private final String containerName;

    public AzureBlobClient(String containerName) {
        this.containerName = containerName;
    }

    public void uploadBlob(String container, String blobName, byte[] content) {
        System.out.printf("[Azure] UPLOAD %s/%s (%d bytes)%n", container, blobName, content.length);
        this.container.put(blobName, content);
    }

    public byte[] downloadBlob(String container, String blobName) {
        System.out.printf("[Azure] DOWNLOAD %s/%s%n", container, blobName);
        return this.container.get(blobName);
    }

    public void deleteBlob(String container, String blobName) {
        System.out.printf("[Azure] DELETE %s/%s%n", container, blobName);
        this.container.remove(blobName);
    }

    public boolean blobExists(String container, String blobName) {
        return this.container.containsKey(blobName);
    }

    public String getContainerName() { return containerName; }
}
