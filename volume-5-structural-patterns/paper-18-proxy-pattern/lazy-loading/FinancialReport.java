import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Real (expensive) report object.
 *
 * Construction is intentionally slow: it simulates the cost of fetching
 * financial data, running calculations, and rendering a PDF. In production
 * this might call an external reporting service or a heavy PDF library.
 */
public class FinancialReport implements Report {

    private final String reportId;
    private final String title;
    private final byte[] pdfContent;
    private final Map<String, String> metadata;

    public FinancialReport(String reportId, String title) {
        this.reportId = reportId;
        this.title    = title;
        System.out.println("  [FinancialReport] Loading report '" + title + "' (ID=" + reportId + ") ...");

        // Simulate expensive PDF generation
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate rendered PDF content
        String pdfText = "%PDF-1.4\n" +
                         "Report: " + title + "\n" +
                         "Generated: " + LocalDateTime.now() + "\n" +
                         "Data: [Q1 Revenue: $1.2M] [Q2 Revenue: $1.5M] [Q3 Revenue: $1.8M]";
        this.pdfContent = pdfText.getBytes(StandardCharsets.UTF_8);

        // Simulate metadata population
        this.metadata = new HashMap<>();
        metadata.put("reportId",     reportId);
        metadata.put("title",        title);
        metadata.put("generatedAt",  LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("author",       "Finance System");
        metadata.put("pageCount",    "12");
        metadata.put("format",       "PDF/A-1b");

        System.out.println("  [FinancialReport] Loaded '" + title + "' — " + pdfContent.length + " bytes");
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public byte[] getPdfContent() {
        return pdfContent;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
