package adapter.storageadapter;

public class S3StorageAdapter implements ObjectStorage {

    private final S3Client client;

    public S3StorageAdapter(S3Client client) {
        this.client = client;
    }

    @Override
    public void upload(String key, byte[] data) {
        client.putObject(client.getBucketName(), key, data);
    }

    @Override
    public byte[] download(String key) {
        return client.getObject(client.getBucketName(), key);
    }

    @Override
    public void delete(String key) {
        client.deleteObject(client.getBucketName(), key);
    }

    @Override
    public boolean exists(String key) {
        return client.objectExists(client.getBucketName(), key);
    }
}
