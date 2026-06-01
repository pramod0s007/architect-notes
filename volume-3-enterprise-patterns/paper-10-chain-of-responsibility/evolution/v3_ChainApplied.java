// v3_ChainApplied.java
// Chain of Responsibility: each stage is a class.
// Adding a stage = one new class + one builder line.
// Two pipelines share the same handlers; composition replaces boolean flags.

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class v3_ChainApplied {

    // ─── Domain model ─────────────────────────────────────────────────────────

    static class FileContext {
        final String  fileName;
        final String  mimeType;
        final long    sizeBytes;
        final boolean isCompressed;

        // Mutable state written by handlers
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

    // ─── FileHandler abstract base ────────────────────────────────────────────

    static abstract class FileHandler {
        private FileHandler next;

        final FileHandler setNext(FileHandler next) {
            this.next = next;
            return next;  // fluent: allows chaining setNext calls
        }

        final ProcessingResult handle(FileContext file) {
            ProcessingResult result = doHandle(file);
            if (result != null) {
                return result;   // this handler stopped the chain (error or final step)
            }
            if (next != null) {
                return next.handle(file);
            }
            return new ProcessingResult(true,
                "Pipeline complete: parsed=" + file.parsedContent
                + (file.indexId != null ? " | indexed=" + file.indexId : "")
                + (file.archivePath != null ? " | archive=" + file.archivePath : ""));
        }

        /**
         * Return non-null to stop the chain (error or terminal success).
         * Return null to pass to the next handler.
         */
        protected abstract ProcessingResult doHandle(FileContext file);

        protected String name() { return getClass().getSimpleName(); }
    }

    // ─── Concrete handlers — each independently testable ─────────────────────

    static class FormatValidationHandler extends FileHandler {
        private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "application/pdf", "text/plain", "application/zip", "application/json"
        ));

        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Validating format...");
            if (file.mimeType == null || file.mimeType.isEmpty()) {
                return new ProcessingResult(false, name() + ": missing MIME type");
            }
            if (!ALLOWED.contains(file.mimeType)) {
                return new ProcessingResult(false, name() + ": unsupported type '" + file.mimeType + "'");
            }
            System.out.println("    -> OK: " + file.mimeType);
            return null;  // pass to next
        }
    }

    static class SizeCheckHandler extends FileHandler {
        private final long maxBytes;
        SizeCheckHandler(long maxBytes) { this.maxBytes = maxBytes; }

        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Checking size...");
            if (file.sizeBytes <= 0) {
                return new ProcessingResult(false, name() + ": file is empty");
            }
            if (file.sizeBytes > maxBytes) {
                return new ProcessingResult(false,
                    name() + ": " + (file.sizeBytes / 1024 / 1024) + " MB exceeds " + (maxBytes / 1024 / 1024) + " MB");
            }
            System.out.println("    -> OK: " + (file.sizeBytes / 1024) + " KB");
            return null;
        }
    }

    static class VirusScanHandler extends FileHandler {
        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Scanning for malware...");
            if (file.fileName.endsWith(".evil")) {
                return new ProcessingResult(false, name() + ": malware detected in " + file.fileName);
            }
            file.isClean = true;
            System.out.println("    -> Clean");
            return null;
        }
    }

    static class DecompressHandler extends FileHandler {
        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Decompressing...");
            if (file.isCompressed && !file.mimeType.equals("application/zip")) {
                return new ProcessingResult(false, name() + ": compressed but MIME is not zip");
            }
            System.out.println("    -> " + (file.isCompressed ? "Decompressed" : "Not compressed, skipped"));
            return null;
        }
    }

    static class ParseHandler extends FileHandler {
        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Parsing content...");
            switch (file.mimeType) {
                case "application/pdf":  file.parsedContent = "[PDF] "  + file.fileName; break;
                case "text/plain":       file.parsedContent = "[TXT] "  + file.fileName; break;
                case "application/json": file.parsedContent = "[JSON] " + file.fileName; break;
                case "application/zip":  file.parsedContent = "[ZIP] "  + file.fileName; break;
                default: return new ProcessingResult(false, name() + ": no parser for " + file.mimeType);
            }
            System.out.println("    -> " + file.parsedContent);
            return null;
        }
    }

    static class EncryptHandler extends FileHandler {
        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Encrypting...");
            file.isEncrypted = true;
            System.out.println("    -> Encrypted (AES-256)");
            return null;
        }
    }

    static class WatermarkHandler extends FileHandler {
        private final String watermarkText;
        WatermarkHandler(String watermarkText) { this.watermarkText = watermarkText; }

        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Watermarking...");
            if ("application/pdf".equals(file.mimeType)) {
                file.watermark = watermarkText;
                System.out.println("    -> Watermarked: " + watermarkText);
            } else {
                System.out.println("    -> Skipped (not a PDF)");
            }
            return null;
        }
    }

    static class IndexHandler extends FileHandler {
        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Indexing...");
            file.indexId = "IDX-" + Math.abs(file.fileName.hashCode());
            System.out.println("    -> Indexed: " + file.indexId);
            return null;
        }
    }

    static class ArchiveHandler extends FileHandler {
        private final String archiveRoot;
        ArchiveHandler(String archiveRoot) { this.archiveRoot = archiveRoot; }

        @Override
        protected ProcessingResult doHandle(FileContext file) {
            System.out.println("  [" + name() + "] Archiving...");
            file.archivePath = archiveRoot + "/" + file.fileName;
            System.out.println("    -> Archived: " + file.archivePath);
            return null;
        }
    }

    // ─── PipelineBuilder — composes handlers without touching their classes ───

    static class PipelineBuilder {
        private final List<FileHandler> handlers = new ArrayList<>();

        PipelineBuilder add(FileHandler handler) {
            handlers.add(handler);
            return this;
        }

        FileHandler build() {
            if (handlers.isEmpty()) throw new IllegalStateException("Pipeline has no handlers");
            for (int i = 0; i < handlers.size() - 1; i++) {
                handlers.get(i).setNext(handlers.get(i + 1));
            }
            return handlers.get(0);  // return head of chain
        }
    }

    // ─── Demo: two pipelines, same handlers, different composition ────────────

    public static void main(String[] args) {

        // Pipeline A: external uploads — full chain including virus scan
        FileHandler externalUploadPipeline = new PipelineBuilder()
            .add(new FormatValidationHandler())
            .add(new SizeCheckHandler(50 * 1024 * 1024L))
            .add(new VirusScanHandler())           // required for external files
            .add(new DecompressHandler())
            .add(new ParseHandler())
            .add(new EncryptHandler())
            .add(new WatermarkHandler("CONFIDENTIAL"))
            .add(new IndexHandler())
            .add(new ArchiveHandler("/archive/external"))
            .build();

        // Pipeline B: trusted internal files — skip virus scan, skip encryption
        FileHandler trustedInternalPipeline = new PipelineBuilder()
            .add(new FormatValidationHandler())
            .add(new SizeCheckHandler(200 * 1024 * 1024L))   // higher size limit
            // NO VirusScanHandler — trusted source
            .add(new DecompressHandler())
            .add(new ParseHandler())
            // NO EncryptHandler — internal files stay unencrypted
            .add(new WatermarkHandler("INTERNAL"))
            .add(new IndexHandler())
            .add(new ArchiveHandler("/archive/internal"))
            .build();

        System.out.println("=== External Upload: report.pdf ===");
        System.out.println(externalUploadPipeline.handle(
            new FileContext("report.pdf", "application/pdf", 1024 * 200, false)));

        System.out.println("\n=== External Upload: infected file ===");
        System.out.println(externalUploadPipeline.handle(
            new FileContext("payload.evil", "application/pdf", 1024, false)));

        System.out.println("\n=== Trusted Internal: large.json ===");
        System.out.println(trustedInternalPipeline.handle(
            new FileContext("data-export.json", "application/json", 1024 * 1024 * 80L, false)));

        System.out.println("\nKey insight:");
        System.out.println("  - Same FormatValidationHandler instance could be reused in both pipelines.");
        System.out.println("  - Adding a 'ChecksumHandler' = 1 new class + 1 builder line.");
        System.out.println("  - No boolean parameters leaking internal stages to callers.");
    }
}
