// SRP — owned by the Analytics / Front-end team.
// Single responsibility: transform raw rows into a target format.
// Data source or delivery changes never touch this class.

import java.util.List;

public class ReportFormatter {

    private static final String CSV_HEADER  = "Product,Region,Revenue";
    private static final String HTML_HEADER =
            "<html><body><h2>Sales Report</h2>" +
            "<table border='1'><tr><th>Product</th><th>Region</th><th>Revenue</th></tr>";
    private static final String HTML_FOOTER = "</table></body></html>";

    public String formatAsCsv(List<String> rows) {
        StringBuilder sb = new StringBuilder(CSV_HEADER).append("\n");
        for (String row : rows) {
            sb.append(row).append("\n");
        }
        System.out.println("[Formatter] Formatted " + rows.size() + " rows as CSV.");
        return sb.toString();
    }

    public String formatAsHtml(List<String> rows) {
        StringBuilder sb = new StringBuilder(HTML_HEADER);
        for (String row : rows) {
            String[] cols = row.split(",");
            sb.append("<tr>");
            for (String col : cols) {
                sb.append("<td>").append(col.trim()).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append(HTML_FOOTER);
        System.out.println("[Formatter] Formatted " + rows.size() + " rows as HTML.");
        return sb.toString();
    }
}
