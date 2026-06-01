package evolution;

/**
 * EVOLUTION v2 — Decorator Pattern Applied
 *
 * Domain: File Reader
 *
 * v1 had 8 classes for 3 capabilities (2^3 combinatorial explosion).
 * v2 has 4 classes total:
 *   - DataReader (interface)
 *   - FileDataReader (base: reads from disk)
 *   - BufferingDecorator
 *   - CompressionDecorator
 *   - EncryptionDecorator
 *
 * Any combination is composed at runtime — no new class needed.
 * Adding a 4th capability (e.g., Logging): 1 new decorator class, zero other changes.
 *
 * Composition examples:
 *   Plain:           new FileDataReader(path)
 *   Buffered:        new BufferingDecorator(new FileDataReader(path))
 *   All three:       new BufferingDecorator(
 *                        new CompressionDecorator(
 *                            new EncryptionDecorator(
 *                                new FileDataReader(path))))
 */
public class v2_DecoratorApplied {

    // ---------------------------------------------------------------
    // Component interface — all decorators and the base implement this
    // ---------------------------------------------------------------
    interface DataReader {
        String read();
        String getDescription();
    }

    // ---------------------------------------------------------------
    // Concrete component — reads raw bytes
    // ---------------------------------------------------------------
    static class FileDataReader implements DataReader {
        private final String path;

        FileDataReader(String path) {
            this.path = path;
        }

        @Override
        public String read() {
            // Simulated: in real code, reads FileInputStream
            return "RAW[" + path + "]";
        }

        @Override
        public String getDescription() {
            return "File(" + path + ")";
        }
    }

    // ---------------------------------------------------------------
    // Abstract decorator — holds a wrapped DataReader
    // ---------------------------------------------------------------
    static abstract class DataReaderDecorator implements DataReader {
        protected final DataReader wrapped;

        DataReaderDecorator(DataReader wrapped) {
            this.wrapped = wrapped;
        }
    }

    // ---------------------------------------------------------------
    // Concrete decorators — each adds exactly one capability
    // ---------------------------------------------------------------

    /** Adds buffering: reads ahead in 8KB chunks for performance. */
    static class BufferingDecorator extends DataReaderDecorator {
        private static final int BUFFER_SIZE_KB = 8;

        BufferingDecorator(DataReader wrapped) {
            super(wrapped);
        }

        @Override
        public String read() {
            // In real code: wrap InputStream in BufferedInputStream(stream, 8192)
            return "BUFFERED[" + BUFFER_SIZE_KB + "KB]{" + wrapped.read() + "}";
        }

        @Override
        public String getDescription() {
            return "Buffered(" + wrapped.getDescription() + ")";
        }
    }

    /** Adds decompression: transparent GZIP/LZ4 decompression. */
    static class CompressionDecorator extends DataReaderDecorator {
        private final String algorithm;

        CompressionDecorator(DataReader wrapped) {
            this(wrapped, "GZIP");
        }

        CompressionDecorator(DataReader wrapped, String algorithm) {
            super(wrapped);
            this.algorithm = algorithm;
        }

        @Override
        public String read() {
            // In real code: wrap InputStream in GZIPInputStream
            return "DECOMPRESS[" + algorithm + "]{" + wrapped.read() + "}";
        }

        @Override
        public String getDescription() {
            return "Compressed[" + algorithm + "](" + wrapped.getDescription() + ")";
        }
    }

    /** Adds decryption: AES-256-GCM transparent decryption. */
    static class EncryptionDecorator extends DataReaderDecorator {
        private final String cipher;

        EncryptionDecorator(DataReader wrapped) {
            this(wrapped, "AES-256-GCM");
        }

        EncryptionDecorator(DataReader wrapped, String cipher) {
            super(wrapped);
            this.cipher = cipher;
        }

        @Override
        public String read() {
            // In real code: wrap InputStream in CipherInputStream
            return "DECRYPT[" + cipher + "]{" + wrapped.read() + "}";
        }

        @Override
        public String getDescription() {
            return "Encrypted[" + cipher + "](" + wrapped.getDescription() + ")";
        }
    }

    // ---------------------------------------------------------------
    // Main — compose any combination without new classes
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v2: Decorator Pattern Applied ===\n");

        String path = "data.bin";

        // Plain file reader
        DataReader plain = new FileDataReader(path);

        // Single capabilities
        DataReader buffered    = new BufferingDecorator(new FileDataReader(path));
        DataReader compressed  = new CompressionDecorator(new FileDataReader(path));
        DataReader encrypted   = new EncryptionDecorator(new FileDataReader(path));

        // Two capabilities composed
        DataReader bufCompressed = new BufferingDecorator(
                new CompressionDecorator(new FileDataReader(path)));
        DataReader bufEncrypted  = new BufferingDecorator(
                new EncryptionDecorator(new FileDataReader(path)));
        DataReader compEncrypted = new CompressionDecorator(
                new EncryptionDecorator(new FileDataReader(path)));

        // All three — composed in reading order: decrypt first, then decompress, then buffer
        DataReader allThree = new BufferingDecorator(
                new CompressionDecorator(
                        new EncryptionDecorator(
                                new FileDataReader(path))));

        DataReader[] readers = {
            plain, buffered, compressed, encrypted,
            bufCompressed, bufEncrypted, compEncrypted, allThree
        };

        for (DataReader reader : readers) {
            System.out.printf("%-62s -> %s%n",
                    reader.getDescription(), reader.read());
        }

        System.out.println();
        System.out.println("=== Comparison ===");
        System.out.println("v1: 8 classes — 1 per combination");
        System.out.println("v2: 4 classes — compose any combination at runtime");
        System.out.println();
        System.out.println("Adding LoggingDecorator (4th capability):");
        System.out.println("  v1: would need 16 classes (2^4)");
        System.out.println("  v2: 1 new decorator class, zero other changes");

        System.out.println();
        System.out.println("--- Demo: inject LoggingDecorator inline with lambda ---");
        // Demonstrate extending with a quick ad-hoc wrapper (lambda cannot implement abstract class,
        // but we can show another concrete decorator inline for illustration)
        DataReader logged = new DataReaderDecorator(allThree) {
            @Override public String read() {
                System.out.println("  [LOG] About to read...");
                String result = wrapped.read();
                System.out.println("  [LOG] Read complete: " + result.length() + " chars");
                return result;
            }
            @Override public String getDescription() {
                return "Logged(" + wrapped.getDescription() + ")";
            }
        };
        System.out.println(logged.getDescription());
        System.out.println(logged.read());
    }
}
