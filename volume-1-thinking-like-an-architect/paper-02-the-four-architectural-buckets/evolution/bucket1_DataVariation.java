import java.util.*;

// =============================================================================
// BUCKET 1: DATA VARIATION — Report Exporter
// =============================================================================
// Symptom : if-else chain growing as new export formats are added
// Pressure : DATA variation — the OUTPUT FORMAT changes, not the algorithm
// Solution : A Map<String, Formatter> or template — NOT Strategy Pattern
//
// Key insight: The algorithm is "traverse report fields and render them".
// That algorithm does not change. Only the serialization vocabulary changes.
// Adding a Strategy interface here is over-engineering — you are not swapping
// behavior, you are swapping a data template/vocabulary.
// =============================================================================

class Report {
    private final String title;
    private final String author;
    private final List<String> rows;

    public Report(String title, String author, List<String> rows) {
        this.title  = title;
        this.author = author;
        this.rows   = rows;
    }
    public String getTitle()       { return title; }
    public String getAuthor()      { return author; }
    public List<String> getRows()  { return rows; }
}

// ---------------------------------------------------------------------------
// v1  (Month 1) — Two formats, dead simple, CORRECT AS-IS. No pressure yet.
// ---------------------------------------------------------------------------
class ReportExporterV1 {

    public String export(Report report, String format) {
        if (format.equals("json")) return toJson(report);
        if (format.equals("csv"))  return toCsv(report);
        throw new IllegalArgumentException("Unknown format: " + format);
    }

    private String toJson(Report r) {
        return "{ \"title\": \"" + r.getTitle() + "\", \"author\": \"" + r.getAuthor() + "\" }";
    }
    private String toCsv(Report r) {
        return "title,author\n" + r.getTitle() + "," + r.getAuthor();
    }
}

// ---------------------------------------------------------------------------
// v2  (Month 7) — xml, pdf, html added. Five format branches, still growing.
//
// PAIN POINTS (data variation signals):
//   [!] Every new format = open this class and add an if-branch
//   [!] All five algorithms follow the same shape: open tag/delimiter,
//       write title, write author, write rows, close tag/delimiter
//   [!] The ALGORITHM has not changed — only the tag vocabulary has changed
//   [!] A new developer asks "should I add a Strategy here?" — NO.
//       The behavior is identical; only the serialization template differs.
//
// Diagnosis: This is DATA variation. The serialization vocabulary is data,
// not behavior. The fix is a map-based registry or a template object, not
// a polymorphic interface hierarchy.
// ---------------------------------------------------------------------------
class ReportExporterV2 {

    public String export(Report report, String format) {
        if (format.equals("json")) return toJson(report);
        if (format.equals("csv"))  return toCsv(report);
        if (format.equals("xml"))  return toXml(report);   // added month 3
        if (format.equals("html")) return toHtml(report);  // added month 5
        if (format.equals("tsv"))  return toTsv(report);   // added month 7
        throw new IllegalArgumentException("Unknown format: " + format);
    }

    private String toJson(Report r) {
        return "{ \"title\": \"" + r.getTitle() + "\", \"author\": \"" + r.getAuthor() + "\" }";
    }
    private String toCsv(Report r) {
        return "title,author\n" + r.getTitle() + "," + r.getAuthor();
    }
    private String toXml(Report r) {
        return "<report><title>" + r.getTitle() + "</title><author>" + r.getAuthor() + "</author></report>";
    }
    private String toHtml(Report r) {
        return "<html><body><h1>" + r.getTitle() + "</h1><p>" + r.getAuthor() + "</p></body></html>";
    }
    private String toTsv(Report r) {
        return "title\tauthor\n" + r.getTitle() + "\t" + r.getAuthor();
    }
}

// ---------------------------------------------------------------------------
// v3  (Refactored) — Map-based registry. No Strategy interface needed.
//
// WHAT CHANGED:
//   - Introduced a @FunctionalInterface Formatter (a data transformer, not a strategy)
//   - Pre-registered all formats in a Map<String, Formatter>
//   - export() no longer has any if-else — it delegates to the map
//
// WHY THIS (not Strategy Pattern):
//   - The algorithm is identical for every format: iterate fields, apply vocabulary
//   - We are parameterizing DATA (open/close markers), not BEHAVIOR
//   - Adding a new format = one register() call, no class, no interface hierarchy
//   - If formats were genuinely computing differently (e.g. a columnar stats summary
//     vs a narrative text), THAT would be behavior variation → use Strategy
// ---------------------------------------------------------------------------
@FunctionalInterface
interface Formatter {
    String format(Report report);
}

class ReportExporterV3 {

    private final Map<String, Formatter> registry = new HashMap<>();

    public ReportExporterV3() {
        register("json", r ->
            "{ \"title\": \"" + r.getTitle() + "\", \"author\": \"" + r.getAuthor() + "\" }");
        register("csv",  r ->
            "title,author\n" + r.getTitle() + "," + r.getAuthor());
        register("xml",  r ->
            "<report><title>" + r.getTitle() + "</title><author>" + r.getAuthor() + "</author></report>");
        register("html", r ->
            "<html><body><h1>" + r.getTitle() + "</h1><p>" + r.getAuthor() + "</p></body></html>");
        register("tsv",  r ->
            "title\tauthor\n" + r.getTitle() + "\t" + r.getAuthor());
    }

    public void register(String format, Formatter formatter) {
        registry.put(format, formatter);
    }

    // No if-else. Open for extension, closed for modification.
    public String export(Report report, String format) {
        Formatter formatter = registry.get(format);
        if (formatter == null) throw new IllegalArgumentException("Unknown format: " + format);
        return formatter.format(report);
    }
}

// ---------------------------------------------------------------------------
// Demo — compiles and runs
// ---------------------------------------------------------------------------
public class bucket1_DataVariation {
    public static void main(String[] args) {
        Report report = new Report("Q1 Sales", "Alice", Arrays.asList("row1", "row2"));

        // v1 — two formats
        ReportExporterV1 v1 = new ReportExporterV1();
        System.out.println("v1 JSON: " + v1.export(report, "json"));

        // v2 — five formats, if-else pain
        ReportExporterV2 v2 = new ReportExporterV2();
        System.out.println("v2 XML : " + v2.export(report, "xml"));

        // v3 — map registry, no if-else, easy to extend
        ReportExporterV3 v3 = new ReportExporterV3();
        System.out.println("v3 HTML: " + v3.export(report, "html"));

        // Adding a new format without touching ReportExporterV3 class body:
        v3.register("markdown", r -> "# " + r.getTitle() + "\nBy " + r.getAuthor());
        System.out.println("v3 MD  : " + v3.export(report, "markdown"));
    }
}
