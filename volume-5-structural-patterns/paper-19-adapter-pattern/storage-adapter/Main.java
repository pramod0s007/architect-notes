package adapter.storageadapter;

import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Using S3 backend ===");
        ObjectStorage storage = new S3StorageAdapter(new S3Client("my-app-bucket"));
        runDemo(storage);

        System.out.println("\n=== Switching to Azure — caller code unchanged ===");
        storage = new AzureBlobStorageAdapter(new AzureBlobClient("my-app-container"));
        runDemo(storage);
    }

    // This method never changes regardless of which storage backend is used
    static void runDemo(ObjectStorage storage) {
        byte[] data = "Hello, Adapter Pattern!".getBytes(StandardCharsets.UTF_8);

        storage.upload("docs/readme.txt", data);
        System.out.println("Exists: " + storage.exists("docs/readme.txt"));

        byte[] downloaded = storage.download("docs/readme.txt");
        System.out.println("Downloaded: " + new String(downloaded, StandardCharsets.UTF_8));

        storage.delete("docs/readme.txt");
        System.out.println("After delete, exists: " + storage.exists("docs/readme.txt"));
    }
}
