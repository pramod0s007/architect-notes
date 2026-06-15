// SRP — owned by the Product / Workflow team.
// Single responsibility: coordinate the report pipeline steps.
// One reason to change: the orchestration flow itself (not data, format, or delivery).

import java.util.List;

public class ReportOrchestrator {

    private final ReportDataFetcher   fetcher;
    private final ReportFormatter     formatter;
    private final ReportDeliveryService delivery;

    public ReportOrchestrator(ReportDataFetcher fetcher,
                              ReportFormatter formatter,
                              ReportDeliveryService delivery) {
        this.fetcher   = fetcher;
        this.formatter = formatter;
        this.delivery  = delivery;
    }

    public void generateAndDeliverReport(String period, String recipient) {
        System.out.println("[Orchestrator] Starting report pipeline for period=" + period);

        List<String> rows = fetcher.fetchSalesData(period);
        if (rows.isEmpty()) {
            System.out.println("[Orchestrator] Aborting — no data available.");
            return;
        }

        String csv      = formatter.formatAsCsv(rows);
        String htmlBody = formatter.formatAsHtml(rows);

        String filename = "sales-report-" + period + ".csv";
        delivery.saveToStorage(filename, csv);
        delivery.emailReport(recipient, htmlBody);

        System.out.println("[Orchestrator] Report pipeline complete for period=" + period);
    }
}
