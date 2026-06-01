import java.util.HashMap;
import java.util.Map;

/**
 * Simulated Amazon S3 storage provider.
 *
 * In production this would use the AWS SDK v2 S3Client. Here we use an
 * in-memory map so the example is runnable without any AWS credentials.
 */
public class S3StorageProvider implements StorageProvider {

    private final String bucketName;
    private final Map<String, byte[]> store = new HashMap<>();

    public S3StorageProvider(String bucketName) {
        this.bucketName = bucketName;
        System.out.println("[S3] Initialized — bucket: " + bucketName);
    }

    @Override
    public void upload(String key, byte[] data) {
        store.put(key, data);
        System.out.println("[S3] PUT s3://" + bucketName + "/" + key
                + " (" + data.length + " bytes)");
    }

    @Override
    public byte[] download(String key) {
        byte[] data = store.get(key);
        if (data == null) {
            System.out.println("[S3] GET s3://" + bucketName + "/" + key + " -> 404 Not Found");
            return null;
        }
        System.out.println("[S3] GET s3://" + bucketName + "/" + key
                + " (" + data.length + " bytes)");
        return data;
    }

    @Override
    public void delete(String key) {
        boolean existed = store.remove(key) != null;
        System.out.println("[S3] DELETE s3://" + bucketName + "/" + key
                + (existed ? " -> deleted" : " -> key not found, no-op"));
    }
}
