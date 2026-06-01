import java.nio.charset.StandardCharsets;

/**
 * Demonstrates the Virtual Proxy pattern for lazy-loading expensive reports.
 *
 * Key observation: listing report titles is instantaneous. Only requesting
 * PDF content or metadata triggers the 500ms expensive load — and only for
 * the specific report that was accessed, not all of them.
 */
public class Main {

    public static void main(String[] args) {

        // ── Build the report cache (no loading yet) ───────────────────────
        System.out.println("=== Registering Reports (no load yet) ===");
        ReportCache reportCache = new ReportCache();
        reportCache.register("RPT-001", "Q1 2024 Financial Summary");
        reportCache.register("RPT-002", "Q2 2024 Financial Summary");
        reportCache.register("RPT-003", "Annual 2023 Audited Report");
        reportCache.register("RPT-004", "Board Meeting Deck — March 2024");

        // ── List titles — cheap, no real objects constructed ─────────────
        System.out.println("\n=== Listing Report Titles (cheap — no PDF loading) ===");
        reportCache.printReportList();

        // ── Access title only — still cheap ──────────────────────────────
        System.out.println("\n=== Reading Titles Directly (still cheap) ===");
        for (String id : new String[]{"RPT-001", "RPT-002", "RPT-003", "RPT-004"}) {
            Report r = reportCache.getReport(id);
            System.out.println("  Title: " + r.getTitle());
        }

        // ── Trigger lazy load for ONE report ─────────────────────────────
        System.out.println("\n=== Requesting PDF for RPT-002 (triggers lazy load) ===");
        long start = System.currentTimeMillis();
        Report q2 = reportCache.getReport("RPT-002");
        byte[] pdf = q2.getPdfContent();
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  PDF loaded in " + elapsed + "ms, size=" + pdf.length + " bytes");
        System.out.println("  First 60 chars: " + new String(pdf, StandardCharsets.UTF_8).substring(0, 60));

        // ── Second access — real object already loaded ────────────────────
        System.out.println("\n=== Second Access to RPT-002 (already loaded) ===");
        start = System.currentTimeMillis();
        byte[] pdf2 = q2.getPdfContent();
        elapsed = System.currentTimeMillis() - start;
        System.out.println("  Retrieved in " + elapsed + "ms (no reload)");

        // ── Metadata access ───────────────────────────────────────────────
        System.out.println("\n=== Reading Metadata for RPT-002 ===");
        q2.getMetadata().forEach((k, v) -> System.out.println("  " + k + ": " + v));

        // ── Show which reports are still unloaded ─────────────────────────
        System.out.println("\n=== Cache State After Accessing Only RPT-002 ===");
        reportCache.printReportList();
        System.out.println("  -> Only RPT-002 was loaded; three reports deferred successfully.");
    }
}
