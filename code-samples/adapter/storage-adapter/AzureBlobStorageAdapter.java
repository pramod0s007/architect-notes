package adapter.storageadapter;

public class AzureBlobStorageAdapter implements ObjectStorage {

    private final AzureBlobClient client;

    public AzureBlobStorageAdapter(AzureBlobClient client) {
        this.client = client;
    }

    @Override
    public void upload(String key, byte[] data) {
        client.uploadBlob(client.getContainerName(), key, data);
    }

    @Override
    public byte[] download(String key) {
        return client.downloadBlob(client.getContainerName(), key);
    }

    @Override
    public void delete(String key) {
        client.deleteBlob(client.getContainerName(), key);
    }

    @Override
    public boolean exists(String key) {
        return client.blobExists(client.getContainerName(), key);
    }
}
