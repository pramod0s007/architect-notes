package evolution;

/**
 * EVOLUTION v1 — Inheritance Explosion (Combinatorial Explosion)
 *
 * Domain: File Reader
 *
 * This is the combinatorial explosion Decorator Pattern solves.
 *
 * Three independent capabilities:
 *   - Buffering    (B): reads in chunks for performance
 *   - Compression  (C): decompress data as it is read
 *   - Encryption   (E): decrypt data as it is read
 *
 * Every combination needs its own class. With 3 binary capabilities:
 *   2^3 = 8 classes required.
 *
 * Adding a 4th capability (e.g., Logging) would need 2^4 = 16 classes.
 * Adding a 5th = 32 classes. The number doubles with each new feature.
 *
 * Class hierarchy:
 *   FileReader
 *   BufferedFileReader
 *   CompressedFileReader
 *   EncryptedFileReader
 *   BufferedCompressedFileReader
 *   BufferedEncryptedFileReader
 *   CompressedEncryptedFileReader
 *   BufferedCompressedEncryptedFileReader
 *
 * None of the 7 subclasses can be independently tested for just their
 * one capability — they all carry the full file-reading responsibility.
 */
public class v1_InheritanceExplosion {

    // ---------------------------------------------------------------
    // Base class
    // ---------------------------------------------------------------

    /** Base: reads raw bytes from a file path. */
    static class FileReader {
        protected final String path;

        FileReader(String path) {
            this.path = path;
        }

        public String read() {
            // Simulated: in reality, reads bytes from disk
            return "RAW[" + path + "]";
        }

        public String getDescription() {
            return "FileReader(" + path + ")";
        }
    }

    // ---------------------------------------------------------------
    // Single-capability subclasses (3 classes)
    // ---------------------------------------------------------------

    static class BufferedFileReader extends FileReader {
        BufferedFileReader(String path) { super(path); }

        @Override
        public String read() {
            return "BUFFERED{" + super.read() + "}";
        }

        @Override public String getDescription() { return "Buffered(" + super.getDescription() + ")"; }
    }

    static class CompressedFileReader extends FileReader {
        CompressedFileReader(String path) { super(path); }

        @Override
        public String read() {
            return "DECOMPRESS{" + super.read() + "}";
        }

        @Override public String getDescription() { return "Compressed(" + super.getDescription() + ")"; }
    }

    static class EncryptedFileReader extends FileReader {
        EncryptedFileReader(String path) { super(path); }

        @Override
        public String read() {
            return "DECRYPT{" + super.read() + "}";
        }

        @Override public String getDescription() { return "Encrypted(" + super.getDescription() + ")"; }
    }

    // ---------------------------------------------------------------
    // Two-capability subclasses (3 more classes)
    // ---------------------------------------------------------------

    static class BufferedCompressedFileReader extends FileReader {
        BufferedCompressedFileReader(String path) { super(path); }

        @Override
        public String read() {
            // Must re-implement both capabilities together — no reuse
            return "BUFFERED{DECOMPRESS{" + "RAW[" + path + "]" + "}}";
        }

        @Override public String getDescription() { return "Buffered+Compressed(" + path + ")"; }
    }

    static class BufferedEncryptedFileReader extends FileReader {
        BufferedEncryptedFileReader(String path) { super(path); }

        @Override
        public String read() {
            return "BUFFERED{DECRYPT{" + "RAW[" + path + "]" + "}}";
        }

        @Override public String getDescription() { return "Buffered+Encrypted(" + path + ")"; }
    }

    static class CompressedEncryptedFileReader extends FileReader {
        CompressedEncryptedFileReader(String path) { super(path); }

        @Override
        public String read() {
            return "DECOMPRESS{DECRYPT{" + "RAW[" + path + "]" + "}}";
        }

        @Override public String getDescription() { return "Compressed+Encrypted(" + path + ")"; }
    }

    // ---------------------------------------------------------------
    // Three-capability subclass (1 more class — the kitchen-sink)
    // ---------------------------------------------------------------

    /** All three capabilities combined — the class no one wants to maintain. */
    static class BufferedCompressedEncryptedFileReader extends FileReader {
        BufferedCompressedEncryptedFileReader(String path) { super(path); }

        @Override
        public String read() {
            // All three capabilities inlined — zero reuse of siblings
            return "BUFFERED{DECOMPRESS{DECRYPT{" + "RAW[" + path + "]" + "}}}";
        }

        @Override public String getDescription() {
            return "Buffered+Compressed+Encrypted(" + path + ")";
        }
    }

    // ---------------------------------------------------------------
    // Main — illustrate the problem
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v1: Inheritance Explosion ===\n");

        String path = "data.bin";

        FileReader[]  readers = {
            new FileReader(path),
            new BufferedFileReader(path),
            new CompressedFileReader(path),
            new EncryptedFileReader(path),
            new BufferedCompressedFileReader(path),
            new BufferedEncryptedFileReader(path),
            new CompressedEncryptedFileReader(path),
            new BufferedCompressedEncryptedFileReader(path)
        };

        for (FileReader reader : readers) {
            System.out.printf("%-50s -> %s%n", reader.getDescription(), reader.read());
        }

        System.out.println();
        System.out.println("8 classes for 3 independent capabilities (2^3).");
        System.out.println("Adding a 4th capability (e.g., Logging) needs 16 classes (2^4).");
        System.out.println("Adding a 5th needs 32. The hierarchy is unmaintainable.");
        System.out.println();
        System.out.println("No combination shares code with another — every subclass");
        System.out.println("re-implements all the capabilities it combines.");
        System.out.println();
        System.out.println("Decorator Pattern (v2) replaces these 8 classes with 4.");
    }
}
