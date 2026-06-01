// v3_VisitorApplied.java
// Visitor pattern: adding a new operation (ExcelExportVisitor) = 1 new class.
// Report types are stable. Operations grow. Visitor wins.

import java.util.Arrays;
import java.util.List;

public class v3_VisitorApplied {

    // ─── Visitor interface ────────────────────────────────────────────────────
    // One method per report type — the compiler forces completeness.

    interface ReportVisitor {
        String visit(BalanceSheet report);
        String visit(IncomeStatement report);
        String visit(CashFlowStatement report);
    }

    // ─── Report interface ─────────────────────────────────────────────────────
    // Each report just calls visitor.visit(this) — no logic, no instanceof.

    interface FinancialReport {
        String accept(ReportVisitor visitor);
    }

    // ─── Concrete report types ────────────────────────────────────────────────

    static class BalanceSheet implements FinancialReport {
        final String companyName;
        final double totalAssets;
        final double totalLiabilities;

        BalanceSheet(String companyName, double totalAssets, double totalLiabilities) {
            this.companyName      = companyName;
            this.totalAssets      = totalAssets;
            this.totalLiabilities = totalLiabilities;
        }

        @Override
        public String accept(ReportVisitor visitor) {
            return visitor.visit(this);   // double dispatch
        }
    }

    static class IncomeStatement implements FinancialReport {
        final String companyName;
        final double revenue;
        final double expenses;

        IncomeStatement(String companyName, double revenue, double expenses) {
            this.companyName = companyName;
            this.revenue     = revenue;
            this.expenses    = expenses;
        }

        @Override
        public String accept(ReportVisitor visitor) {
            return visitor.visit(this);
        }
    }

    static class CashFlowStatement implements FinancialReport {
        final String companyName;
        final double operatingCashFlow;
        final double investingCashFlow;
        final double financingCashFlow;

        CashFlowStatement(String companyName,
                          double operating, double investing, double financing) {
            this.companyName       = companyName;
            this.operatingCashFlow = operating;
            this.investingCashFlow = investing;
            this.financingCashFlow = financing;
        }

        @Override
        public String accept(ReportVisitor visitor) {
            return visitor.visit(this);
        }
    }

    // ─── Operation 1: PDF Renderer ────────────────────────────────────────────

    static class PdfRendererVisitor implements ReportVisitor {
        @Override
        public String visit(BalanceSheet r) {
            return "[PDF] Balance Sheet — " + r.companyName
                + " | Assets: $" + r.totalAssets
                + " | Liabilities: $" + r.totalLiabilities
                + " | Equity: $" + (r.totalAssets - r.totalLiabilities);
        }

        @Override
        public String visit(IncomeStatement r) {
            return "[PDF] Income Statement — " + r.companyName
                + " | Revenue: $" + r.revenue
                + " | Expenses: $" + r.expenses
                + " | Net Income: $" + (r.revenue - r.expenses);
        }

        @Override
        public String visit(CashFlowStatement r) {
            return "[PDF] Cash Flow — " + r.companyName
                + " | Operating: $" + r.operatingCashFlow
                + " | Investing: $" + r.investingCashFlow
                + " | Financing: $" + r.financingCashFlow;
        }
    }

    // ─── Operation 2: HTML Renderer ───────────────────────────────────────────

    static class HtmlRendererVisitor implements ReportVisitor {
        @Override
        public String visit(BalanceSheet r) {
            return "<section><h1>Balance Sheet – " + r.companyName + "</h1>"
                + "<p>Assets: $" + r.totalAssets + "</p>"
                + "<p>Liabilities: $" + r.totalLiabilities + "</p>"
                + "<p>Equity: $" + (r.totalAssets - r.totalLiabilities) + "</p></section>";
        }

        @Override
        public String visit(IncomeStatement r) {
            return "<section><h1>Income Statement – " + r.companyName + "</h1>"
                + "<p>Revenue: $" + r.revenue + "</p>"
                + "<p>Expenses: $" + r.expenses + "</p>"
                + "<p>Net Income: $" + (r.revenue - r.expenses) + "</p></section>";
        }

        @Override
        public String visit(CashFlowStatement r) {
            return "<section><h1>Cash Flow – " + r.companyName + "</h1>"
                + "<p>Operating: $" + r.operatingCashFlow + "</p>"
                + "<p>Investing: $" + r.investingCashFlow + "</p>"
                + "<p>Financing: $" + r.financingCashFlow + "</p></section>";
        }
    }

    // ─── Operation 3: Excel Export — added with ZERO changes to existing code ─

    static class ExcelExportVisitor implements ReportVisitor {
        @Override
        public String visit(BalanceSheet r) {
            return "[XLSX] BalanceSheet," + r.companyName
                + "," + r.totalAssets + "," + r.totalLiabilities
                + "," + (r.totalAssets - r.totalLiabilities);
        }

        @Override
        public String visit(IncomeStatement r) {
            return "[XLSX] IncomeStatement," + r.companyName
                + "," + r.revenue + "," + r.expenses
                + "," + (r.revenue - r.expenses);
        }

        @Override
        public String visit(CashFlowStatement r) {
            return "[XLSX] CashFlow," + r.companyName
                + "," + r.operatingCashFlow
                + "," + r.investingCashFlow
                + "," + r.financingCashFlow;
        }
    }

    // ─── Demo ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        List<FinancialReport> reports = Arrays.asList(
            new BalanceSheet("Acme Corp", 500_000, 200_000),
            new IncomeStatement("Acme Corp", 1_000_000, 750_000),
            new CashFlowStatement("Acme Corp", 80_000, -30_000, -20_000)
        );

        ReportVisitor pdf   = new PdfRendererVisitor();
        ReportVisitor html  = new HtmlRendererVisitor();
        ReportVisitor excel = new ExcelExportVisitor();

        System.out.println("=== PDF Output ===");
        reports.forEach(r -> System.out.println(r.accept(pdf)));

        System.out.println("\n=== HTML Output ===");
        reports.forEach(r -> System.out.println(r.accept(html)));

        System.out.println("\n=== Excel Export (new operation — zero changes to existing classes) ===");
        reports.forEach(r -> System.out.println(r.accept(excel)));
    }
}
