// v2_PartialRefactor.java
// Attempted fix: pull out a FinancialReportRenderer class.
// Problem: it still uses instanceof chains. Adding a new report type means editing
// the renderer class. Adding a new operation means adding another method with another
// instanceof chain. Operations are still coupled to the class hierarchy.

import java.util.ArrayList;
import java.util.List;

public class v2_PartialRefactor {

    // --- Domain objects (cleaned up — no rendering logic) ---

    static class BalanceSheet {
        String companyName;
        double totalAssets;
        double totalLiabilities;
        BalanceSheet(String c, double a, double l) {
            companyName = c; totalAssets = a; totalLiabilities = l;
        }
    }

    static class IncomeStatement {
        String companyName;
        double revenue;
        double expenses;
        IncomeStatement(String c, double r, double e) {
            companyName = c; revenue = r; expenses = e;
        }
    }

    static class CashFlowStatement {
        String companyName;
        double operatingCashFlow;
        double investingCashFlow;
        double financingCashFlow;
        CashFlowStatement(String c, double op, double inv, double fin) {
            companyName = c;
            operatingCashFlow = op; investingCashFlow = inv; financingCashFlow = fin;
        }
    }

    // --- Renderer (operations live here now, but instanceof chains are ugly) ---

    static class FinancialReportRenderer {

        // PDF operation: one big instanceof chain
        String renderAsPdf(Object report) {
            if (report instanceof BalanceSheet) {
                BalanceSheet bs = (BalanceSheet) report;
                return "[PDF] Balance Sheet for " + bs.companyName
                    + " | Assets: $" + bs.totalAssets
                    + " | Equity: $" + (bs.totalAssets - bs.totalLiabilities);
            } else if (report instanceof IncomeStatement) {
                IncomeStatement is = (IncomeStatement) report;
                return "[PDF] Income Statement for " + is.companyName
                    + " | Revenue: $" + is.revenue
                    + " | Net: $" + (is.revenue - is.expenses);
            } else if (report instanceof CashFlowStatement) {
                CashFlowStatement cf = (CashFlowStatement) report;
                return "[PDF] Cash Flow for " + cf.companyName
                    + " | Operating: $" + cf.operatingCashFlow;
            }
            throw new IllegalArgumentException("Unknown report type: " + report.getClass());
        }

        // HTML operation: another instanceof chain — copy-paste of the structure
        String renderAsHtml(Object report) {
            if (report instanceof BalanceSheet) {
                BalanceSheet bs = (BalanceSheet) report;
                return "<h1>Balance Sheet – " + bs.companyName + "</h1>"
                    + "<p>Assets: $" + bs.totalAssets + "</p>";
            } else if (report instanceof IncomeStatement) {
                IncomeStatement is = (IncomeStatement) report;
                return "<h1>Income Statement – " + is.companyName + "</h1>"
                    + "<p>Revenue: $" + is.revenue + "</p>";
            } else if (report instanceof CashFlowStatement) {
                CashFlowStatement cf = (CashFlowStatement) report;
                return "<h1>Cash Flow – " + cf.companyName + "</h1>"
                    + "<p>Operating: $" + cf.operatingCashFlow + "</p>";
            }
            throw new IllegalArgumentException("Unknown report type: " + report.getClass());
        }

        // Adding ExcelExport requires writing a THIRD instanceof chain here.
        // Each new operation = another method with the same shape.
        // The renderer keeps growing and is never closed to modification.
    }

    public static void main(String[] args) {
        List<Object> reports = new ArrayList<>();
        reports.add(new BalanceSheet("Acme Corp", 500_000, 200_000));
        reports.add(new IncomeStatement("Acme Corp", 1_000_000, 750_000));
        reports.add(new CashFlowStatement("Acme Corp", 80_000, -30_000, -20_000));

        FinancialReportRenderer renderer = new FinancialReportRenderer();

        System.out.println("=== PDF Rendering ===");
        for (Object r : reports) System.out.println(renderer.renderAsPdf(r));

        System.out.println("\n=== HTML Rendering ===");
        for (Object r : reports) System.out.println(renderer.renderAsHtml(r));

        System.out.println("\n--- Problem: adding ExcelExportVisitor = editing this class AGAIN ---");
    }
}
