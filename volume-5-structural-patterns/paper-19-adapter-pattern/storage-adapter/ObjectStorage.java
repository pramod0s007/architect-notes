package adapter.storageadapter;

public interface ObjectStorage {
    void upload(String key, byte[] data);
    byte[] download(String key);
    void delete(String key);
    boolean exists(String key);
}
