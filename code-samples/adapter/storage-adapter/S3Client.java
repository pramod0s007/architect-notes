package adapter.storageadapter;

import java.util.HashMap;
import java.util.Map;

// Simulates AWS S3 SDK — incompatible interface
public class S3Client {

    private final Map<String, byte[]> bucket = new HashMap<>();
    private final String bucketName;

    public S3Client(String bucketName) {
        this.bucketName = bucketName;
    }

    public void putObject(String bucket, String key, byte[] body) {
        System.out.printf("[S3] PUT s3://%s/%s (%d bytes)%n", bucket, key, body.length);
        this.bucket.put(key, body);
    }

    public byte[] getObject(String bucket, String key) {
        System.out.printf("[S3] GET s3://%s/%s%n", bucket, key);
        return this.bucket.get(key);
    }

    public void deleteObject(String bucket, String key) {
        System.out.printf("[S3] DELETE s3://%s/%s%n", bucket, key);
        this.bucket.remove(key);
    }

    public boolean objectExists(String bucket, String key) {
        return this.bucket.containsKey(key);
    }

    public String getBucketName() { return bucketName; }
}
