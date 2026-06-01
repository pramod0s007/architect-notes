// v1_MonolithicProcessor.java
// FileProcessor.process() does everything in sequence — validate, size-check,
// virus-scan, decompress, parse. ~80 lines in one method.

public class v1_MonolithicProcessor {

    // ─── Domain model ─────────────────────────────────────────────────────────

    static class FileContext {
        final String fileName;
        final String mimeType;
        final long   sizeBytes;
        final boolean isCompressed;
        boolean      isClean;   // set by virus scan
        String       parsedContent;

        FileContext(String fileName, String mimeType, long sizeBytes, boolean isCompressed) {
            this.fileName     = fileName;
            this.mimeType     = mimeType;
            this.sizeBytes    = sizeBytes;
            this.isCompressed = isCompressed;
            this.isClean      = false;
            this.parsedContent = null;
        }
    }

    static class ProcessingResult {
        final boolean success;
        final String  message;
        ProcessingResult(boolean success, String message) {
            this.success = success; this.message = message;
        }
        @Override public String toString() {
            return (success ? "OK" : "FAIL") + " | " + message;
        }
    }

    // ─── The monolithic processor ─────────────────────────────────────────────

    static class FileProcessor {

        static final long MAX_SIZE_BYTES = 50 * 1024 * 1024L; // 50 MB
        static final java.util.Set<String> ALLOWED_TYPES = new java.util.HashSet<>(
            java.util.Arrays.asList("application/pdf", "text/plain",
                                    "application/zip", "application/json")
        );

        ProcessingResult process(FileContext file) {
            System.out.println("[FileProcessor] Processing: " + file.fileName);

            // ---- Stage 1: Validate format ----
            System.out.println("  [Stage 1] Validating format...");
            if (file.mimeType == null || file.mimeType.isEmpty()) {
                return new ProcessingResult(false, "Format validation failed: missing MIME type");
            }
            if (!ALLOWED_TYPES.contains(file.mimeType)) {
                return new ProcessingResult(false,
                    "Format validation failed: unsupported type '" + file.mimeType + "'");
            }
            System.out.println("  [Stage 1] Format OK: " + file.mimeType);

            // ---- Stage 2: Check file size ----
            System.out.println("  [Stage 2] Checking size...");
            if (file.sizeBytes <= 0) {
                return new ProcessingResult(false, "Size check failed: file is empty");
            }
            if (file.sizeBytes > MAX_SIZE_BYTES) {
                return new ProcessingResult(false,
                    "Size check failed: " + (file.sizeBytes / 1024 / 1024) + " MB exceeds 50 MB limit");
            }
            System.out.println("  [Stage 2] Size OK: " + (file.sizeBytes / 1024) + " KB");

            // ---- Stage 3: Virus scan ----
            System.out.println("  [Stage 3] Running virus scan...");
            // Simulate: files ending in ".evil" are infected
            if (file.fileName.endsWith(".evil")) {
                return new ProcessingResult(false, "Virus scan failed: malware detected");
            }
            file.isClean = true;
            System.out.println("  [Stage 3] Scan clear");

            // ---- Stage 4: Decompress if needed ----
            System.out.println("  [Stage 4] Decompressing...");
            if (file.isCompressed) {
                if (!file.mimeType.equals("application/zip")) {
                    return new ProcessingResult(false,
                        "Decompress failed: file marked compressed but MIME is not zip");
                }
                System.out.println("  [Stage 4] Decompressed OK");
            } else {
                System.out.println("  [Stage 4] Not compressed, skipping");
            }

            // ---- Stage 5: Parse content ----
            System.out.println("  [Stage 5] Parsing content...");
            switch (file.mimeType) {
                case "application/pdf":
                    file.parsedContent = "[PDF parsed] " + file.fileName;
                    break;
                case "text/plain":
                    file.parsedContent = "[TXT parsed] " + file.fileName;
                    break;
                case "application/json":
                    file.parsedContent = "[JSON parsed] " + file.fileName;
                    break;
                case "application/zip":
                    file.parsedContent = "[ZIP contents extracted] " + file.fileName;
                    break;
                default:
                    return new ProcessingResult(false, "Parse failed: no parser for " + file.mimeType);
            }
            System.out.println("  [Stage 5] Parsed: " + file.parsedContent);

            return new ProcessingResult(true, "Pipeline complete: " + file.parsedContent);
        }
    }

    public static void main(String[] args) {
        FileProcessor processor = new FileProcessor();

        System.out.println("=== Valid PDF ===");
        System.out.println(processor.process(
            new FileContext("report.pdf", "application/pdf", 1024 * 512, false)));

        System.out.println("\n=== Oversized file ===");
        System.out.println(processor.process(
            new FileContext("dump.log", "text/plain", 100 * 1024 * 1024L, false)));

        System.out.println("\n=== Infected file ===");
        System.out.println(processor.process(
            new FileContext("malware.evil", "application/pdf", 1024, false)));

        System.out.println("\n--- Adding 'encrypt' or 'watermark' stages means editing this 80-line method. ---");
    }
}
