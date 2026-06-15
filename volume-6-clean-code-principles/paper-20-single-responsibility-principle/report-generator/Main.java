public class Main {

    public static void main(String[] args) {

        ReportDataFetcher    fetcher   = new ReportDataFetcher();
        ReportFormatter      formatter = new ReportFormatter();
        ReportDeliveryService delivery = new ReportDeliveryService();
        ReportOrchestrator   orchestrator =
                new ReportOrchestrator(fetcher, formatter, delivery);

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Report Pipeline — 2024-Q1");
        System.out.println("══════════════════════════════════════════\n");
        orchestrator.generateAndDeliverReport("2024-Q1", "cfo@acmecorp.com");

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Report Pipeline — 2024-Q2");
        System.out.println("══════════════════════════════════════════\n");
        orchestrator.generateAndDeliverReport("2024-Q2", "analytics@acmecorp.com");

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Report Pipeline — unknown period");
        System.out.println("══════════════════════════════════════════\n");
        orchestrator.generateAndDeliverReport("2023-Q4", "cfo@acmecorp.com");
    }
}
