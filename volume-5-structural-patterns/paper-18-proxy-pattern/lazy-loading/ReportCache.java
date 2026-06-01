import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry that holds {@link LazyReportProxy} instances keyed by report ID.
 *
 * Callers can list all reports and read their titles without paying the cost
 * of loading any of them. Individual reports are only materialised when a
 * caller calls {@link Report#getPdfContent()} or {@link Report#getMetadata()}.
 */
public class ReportCache {

    private final Map<String, LazyReportProxy> cache = new LinkedHashMap<>();

    /**
     * Register a report. Only the ID and title are stored; no loading occurs.
     */
    public void register(String reportId, String title) {
        cache.put(reportId, new LazyReportProxy(reportId, title));
    }

    /**
     * Look up a report by ID. Returns the proxy (which may or may not have
     * been loaded yet).
     */
    public Report getReport(String reportId) {
        LazyReportProxy proxy = cache.get(reportId);
        if (proxy == null) {
            throw new IllegalArgumentException("Unknown report ID: " + reportId);
        }
        return proxy;
    }

    /**
     * List all report titles cheaply — no heavy loading triggered.
     */
    public void printReportList() {
        System.out.println("  Available reports (" + cache.size() + "):");
        cache.forEach((id, proxy) ->
            System.out.println("    [" + id + "] " + proxy.getTitle()
                    + (proxy.isLoaded() ? " (loaded)" : " (not loaded)")));
    }

    public Collection<LazyReportProxy> getAllProxies() {
        return Collections.unmodifiableCollection(cache.values());
    }
}
