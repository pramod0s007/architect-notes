// v1_ScatteredMethods.java
// Problem: 3 report types x 2 rendering operations = 6 scattered methods.
// Adding a new operation (Excel export) means touching ALL THREE report classes.

public class v1_ScatteredMethods {

    // --- Domain model ---

    static class BalanceSheet {
        String companyName;
        double totalAssets;
        double totalLiabilities;

        BalanceSheet(String companyName, double totalAssets, double totalLiabilities) {
            this.companyName  = companyName;
            this.totalAssets  = totalAssets;
            this.totalLiabilities = totalLiabilities;
        }

        // Operation 1 mixed into domain object
        String renderAsPdf() {
            return "[PDF] Balance Sheet for " + companyName
                + " | Assets: $" + totalAssets
                + " | Liabilities: $" + totalLiabilities
                + " | Equity: $" + (totalAssets - totalLiabilities);
        }

        // Operation 2 mixed into domain object
        String renderAsHtml() {
            return "<h1>Balance Sheet – " + companyName + "</h1>"
                + "<p>Assets: $" + totalAssets + "</p>"
                + "<p>Liabilities: $" + totalLiabilities + "</p>"
                + "<p>Equity: $" + (totalAssets - totalLiabilities) + "</p>";
        }
    }

    static class IncomeStatement {
        String companyName;
        double revenue;
        double expenses;

        IncomeStatement(String companyName, double revenue, double expenses) {
            this.companyName = companyName;
            this.revenue     = revenue;
            this.expenses    = expenses;
        }

        String renderAsPdf() {
            return "[PDF] Income Statement for " + companyName
                + " | Revenue: $" + revenue
                + " | Expenses: $" + expenses
                + " | Net Income: $" + (revenue - expenses);
        }

        String renderAsHtml() {
            return "<h1>Income Statement – " + companyName + "</h1>"
                + "<p>Revenue: $" + revenue + "</p>"
                + "<p>Expenses: $" + expenses + "</p>"
                + "<p>Net Income: $" + (revenue - expenses) + "</p>";
        }
    }

    static class CashFlowStatement {
        String companyName;
        double operatingCashFlow;
        double investingCashFlow;
        double financingCashFlow;

        CashFlowStatement(String companyName,
                          double operating, double investing, double financing) {
            this.companyName       = companyName;
            this.operatingCashFlow = operating;
            this.investingCashFlow = investing;
            this.financingCashFlow = financing;
        }

        String renderAsPdf() {
            return "[PDF] Cash Flow for " + companyName
                + " | Operating: $" + operatingCashFlow
                + " | Investing: $" + investingCashFlow
                + " | Financing: $" + financingCashFlow;
        }

        String renderAsHtml() {
            return "<h1>Cash Flow – " + companyName + "</h1>"
                + "<p>Operating: $" + operatingCashFlow + "</p>"
                + "<p>Investing: $" + investingCashFlow + "</p>"
                + "<p>Financing: $" + financingCashFlow + "</p>";
        }
    }

    // --- Demo ---

    public static void main(String[] args) {
        BalanceSheet    bs = new BalanceSheet("Acme Corp", 500_000, 200_000);
        IncomeStatement is = new IncomeStatement("Acme Corp", 1_000_000, 750_000);
        CashFlowStatement cf = new CashFlowStatement("Acme Corp", 80_000, -30_000, -20_000);

        System.out.println("=== PDF Rendering ===");
        System.out.println(bs.renderAsPdf());
        System.out.println(is.renderAsPdf());
        System.out.println(cf.renderAsPdf());

        System.out.println("\n=== HTML Rendering ===");
        System.out.println(bs.renderAsHtml());
        System.out.println(is.renderAsHtml());
        System.out.println(cf.renderAsHtml());

        System.out.println("\n--- Problem: adding ExcelExport requires editing all 3 classes ---");
    }
}
