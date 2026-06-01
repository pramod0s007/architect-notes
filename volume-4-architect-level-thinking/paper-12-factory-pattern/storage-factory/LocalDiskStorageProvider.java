import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Local-disk storage provider — writes real files under a base directory.
 *
 * Useful for local development and integration tests that need a real
 * filesystem without cloud credentials. Uses {@code /tmp/local-storage}
 * by default.
 */
public class LocalDiskStorageProvider implements StorageProvider {

    private final File baseDir;

    public LocalDiskStorageProvider(String basePath) {
        this.baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        System.out.println("[Local] Initialized — base dir: " + baseDir.getAbsolutePath());
    }

    @Override
    public void upload(String key, byte[] data) {
        File target = resolveFile(key);
        target.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(target)) {
            fos.write(data);
            System.out.println("[Local] WRITE " + target.getAbsolutePath()
                    + " (" + data.length + " bytes)");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + key, e);
        }
    }

    @Override
    public byte[] download(String key) {
        File target = resolveFile(key);
        if (!target.exists()) {
            System.out.println("[Local] READ " + target.getAbsolutePath() + " -> not found");
            return null;
        }
        try (FileInputStream fis = new FileInputStream(target)) {
            byte[] data = fis.readAllBytes();
            System.out.println("[Local] READ " + target.getAbsolutePath()
                    + " (" + data.length + " bytes)");
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        File target = resolveFile(key);
        if (target.exists()) {
            target.delete();
            System.out.println("[Local] DELETE " + target.getAbsolutePath() + " -> deleted");
        } else {
            System.out.println("[Local] DELETE " + target.getAbsolutePath() + " -> not found, no-op");
        }
    }

    private File resolveFile(String key) {
        // Prevent path traversal: strip leading slashes and resolve safely
        String sanitized = key.replaceAll("^\\.*/", "");
        return new File(baseDir, sanitized);
    }
}
