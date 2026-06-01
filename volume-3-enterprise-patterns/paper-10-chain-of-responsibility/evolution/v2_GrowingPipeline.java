// v2_GrowingPipeline.java
// Four more stages added: encrypt, watermark, index, archive.
// process() is now ~200 lines. It has 9 early-return exit points.
// Adding a new stage requires finding the "right place" inside this wall of code.
// Reordering stages means untangling interleaved state mutations.

public class v2_GrowingPipeline {

    static class FileContext {
        final String fileName;
        final String mimeType;
        final long   sizeBytes;
        final boolean isCompressed;
        boolean isClean;
        String  parsedContent;
        boolean isEncrypted;
        String  watermark;
        String  indexId;
        String  archivePath;

        FileContext(String fileName, String mimeType, long sizeBytes, boolean isCompressed) {
            this.fileName     = fileName;
            this.mimeType     = mimeType;
            this.sizeBytes    = sizeBytes;
            this.isCompressed = isCompressed;
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

    static class FileProcessor {

        static final long MAX_SIZE_BYTES = 50 * 1024 * 1024L;
        static final java.util.Set<String> ALLOWED_TYPES = new java.util.HashSet<>(
            java.util.Arrays.asList("application/pdf", "text/plain",
                                    "application/zip", "application/json")
        );

        /**
         * This method started at 80 lines and is now 200.
         * It has 9 distinct return points.
         * Every new stage is inserted somewhere in the middle.
         * To "skip" virus scan for trusted files, you add a boolean parameter —
         * and now callers must know about internal pipeline stages.
         */
        ProcessingResult process(FileContext file, boolean skipVirusScan,
                                 boolean skipEncryption, boolean skipWatermark) {
            System.out.println("[FileProcessor] Processing: " + file.fileName);

            // Stage 1: Validate format
            System.out.println("  [1/9] Validating format...");
            if (file.mimeType == null || file.mimeType.isEmpty()) {
                return new ProcessingResult(false, "Format validation failed");
            }
            if (!ALLOWED_TYPES.contains(file.mimeType)) {
                return new ProcessingResult(false, "Unsupported type: " + file.mimeType);
            }

            // Stage 2: Check size
            System.out.println("  [2/9] Checking size...");
            if (file.sizeBytes <= 0 || file.sizeBytes > MAX_SIZE_BYTES) {
                return new ProcessingResult(false, "Size check failed: " + file.sizeBytes + " bytes");
            }

            // Stage 3: Virus scan (conditionally skipped — boolean leaks internals)
            if (!skipVirusScan) {
                System.out.println("  [3/9] Virus scan...");
                if (file.fileName.endsWith(".evil")) {
                    return new ProcessingResult(false, "Malware detected");
                }
                file.isClean = true;
            } else {
                System.out.println("  [3/9] Virus scan SKIPPED (trusted source)");
                file.isClean = true;
            }

            // Stage 4: Decompress
            System.out.println("  [4/9] Decompressing...");
            if (file.isCompressed && !file.mimeType.equals("application/zip")) {
                return new ProcessingResult(false, "Decompress failed: wrong MIME for compressed file");
            }

            // Stage 5: Parse
            System.out.println("  [5/9] Parsing...");
            switch (file.mimeType) {
                case "application/pdf":  file.parsedContent = "[PDF] " + file.fileName; break;
                case "text/plain":       file.parsedContent = "[TXT] " + file.fileName; break;
                case "application/json": file.parsedContent = "[JSON] " + file.fileName; break;
                case "application/zip":  file.parsedContent = "[ZIP] " + file.fileName; break;
                default: return new ProcessingResult(false, "No parser for: " + file.mimeType);
            }

            // Stage 6: Encrypt (NEW — conditionally skipped, another boolean)
            if (!skipEncryption) {
                System.out.println("  [6/9] Encrypting...");
                file.isEncrypted = true;
                // imagine AES-256 here
            } else {
                System.out.println("  [6/9] Encryption SKIPPED");
            }

            // Stage 7: Watermark (NEW — only PDFs)
            if (!skipWatermark) {
                System.out.println("  [7/9] Watermarking...");
                if ("application/pdf".equals(file.mimeType)) {
                    file.watermark = "CONFIDENTIAL";
                } else {
                    System.out.println("  [7/9] Watermark skipped (not a PDF)");
                }
            } else {
                System.out.println("  [7/9] Watermark SKIPPED");
            }

            // Stage 8: Index (NEW)
            System.out.println("  [8/9] Indexing...");
            file.indexId = "IDX-" + System.currentTimeMillis();

            // Stage 9: Archive (NEW)
            System.out.println("  [9/9] Archiving...");
            file.archivePath = "/archive/2025/" + file.fileName;

            return new ProcessingResult(true,
                "Done | parsed=" + file.parsedContent
                + " | indexed=" + file.indexId
                + " | archive=" + file.archivePath);
        }
    }

    public static void main(String[] args) {
        FileProcessor processor = new FileProcessor();

        // External uploads: full pipeline
        System.out.println("=== External upload (full pipeline) ===");
        System.out.println(processor.process(
            new FileContext("upload.pdf", "application/pdf", 1024 * 200, false),
            false, false, false
        ));

        // Internal trusted: skip virus scan and encryption
        // Callers must know about internal stages to use the boolean flags.
        System.out.println("\n=== Internal file (skip virus + encryption) ===");
        System.out.println(processor.process(
            new FileContext("report.pdf", "application/pdf", 1024 * 100, false),
            true, true, false
        ));

        System.out.println("\n--- Three boolean parameters leak internal stages.");
        System.out.println("    Adding stage 10 = edit a 200-line method + add a 4th boolean. ---");
    }
}
