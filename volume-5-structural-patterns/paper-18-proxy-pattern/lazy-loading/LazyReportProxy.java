import java.util.Map;

/**
 * Virtual proxy for {@link FinancialReport}.
 *
 * Holds the report ID and title (cheap data) immediately. The real
 * {@link FinancialReport} object is not constructed until the first call to
 * {@link #getPdfContent()} or {@link #getMetadata()}, which are the
 * operations that actually require the expensive load.
 *
 * Thread-safe: uses double-checked locking for the lazy initialisation.
 */
public class LazyReportProxy implements Report {

    private final String reportId;
    private final String title;

    // Volatile for double-checked locking correctness
    private volatile FinancialReport realReport;

    public LazyReportProxy(String reportId, String title) {
        this.reportId = reportId;
        this.title    = title;
        System.out.println("  [LazyReportProxy] Created proxy for '" + title + "' (not loaded yet)");
    }

    /**
     * Title is immediately available — no load needed.
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Triggers load on first call.
     */
    @Override
    public byte[] getPdfContent() {
        return getRealReport().getPdfContent();
    }

    /**
     * Triggers load on first call.
     */
    @Override
    public Map<String, String> getMetadata() {
        return getRealReport().getMetadata();
    }

    public boolean isLoaded() {
        return realReport != null;
    }

    // ── Double-checked locking ────────────────────────────────────────────

    private FinancialReport getRealReport() {
        if (realReport == null) {
            synchronized (this) {
                if (realReport == null) {
                    System.out.println("  [LazyReportProxy] Triggering lazy load for '" + title + "'");
                    realReport = new FinancialReport(reportId, title);
                }
            }
        }
        return realReport;
    }
}
