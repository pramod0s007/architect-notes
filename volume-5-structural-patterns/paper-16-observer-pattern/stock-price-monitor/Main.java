import java.util.Map;

/**
 * Run: javac *.java && java Main
 *
 * Simulates a live market feed and shows how three independent observers
 * each react to the same stream of price events without coupling to each other.
 */
public final class Main {

    public static void main(String[] args) {

        // ── Publisher ─────────────────────────────────────────────────────────
        StockPricePublisher market = new StockPricePublisher();

        // ── Observers ─────────────────────────────────────────────────────────
        PriceAlertObserver alertSystem = new PriceAlertObserver(5.0);  // alert on ≥5% move

        PortfolioObserver alicePortfolio = new PortfolioObserver(
                "Alice",
                Map.of("AAPL", 50, "MSFT", 30));

        PortfolioObserver bobPortfolio = new PortfolioObserver(
                "Bob",
                Map.of("MSFT", 100, "TSLA", 20));

        AuditLogObserver auditLog = new AuditLogObserver();

        market.registerObserver(alertSystem);
        market.registerObserver(alicePortfolio);
        market.registerObserver(bobPortfolio);
        market.registerObserver(auditLog);

        // ── Seed initial prices (no notification — same as previous) ──────────
        market.updatePrice("AAPL", 185.00);
        market.updatePrice("MSFT", 415.00);
        market.updatePrice("TSLA", 245.00);

        // ── Simulate market events ────────────────────────────────────────────
        System.out.println(">>> AAPL earnings beat — price jumps 7%");
        market.updatePrice("AAPL", 197.95);

        System.out.println("\n>>> MSFT minor drift");
        market.updatePrice("MSFT", 417.50);

        System.out.println("\n>>> TSLA recall news — drops 12%");
        market.updatePrice("TSLA", 215.60);

        System.out.println("\n>>> Bob unregisters from market feed");
        market.removeObserver(bobPortfolio);

        System.out.println("\n>>> MSFT post-market surge");
        market.updatePrice("MSFT", 430.00);

        // ── Audit summary ─────────────────────────────────────────────────────
        System.out.println("\n=== Audit log (" + auditLog.getLog().size() + " entries) ===");
        auditLog.getLog().forEach(entry -> System.out.println("  " + entry));
    }
}
