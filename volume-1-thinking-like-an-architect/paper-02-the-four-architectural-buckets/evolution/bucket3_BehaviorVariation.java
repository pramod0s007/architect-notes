import java.util.*;
import java.util.stream.Collectors;

// =============================================================================
// BUCKET 3: BEHAVIOR VARIATION — Report Summarizer
// =============================================================================
// Symptom : if-else chain growing as new summarization styles are added
// Pressure : BEHAVIOR variation — the ALGORITHM itself changes per style
// Solution : Strategy Pattern — this is the correct use case
//
// Key insight: Unlike Bucket 1 (same algorithm, different data vocabulary)
// and Bucket 2 (same workflow, different storage resource), here each
// summarization style performs a genuinely different COMPUTATION:
//   - brief:     extract first sentence of each section header
//   - detailed:  full narrative with statistics
//   - executive: KPIs only, formatted as bullet points
//   - visual:    ASCII chart of row counts per section
//   - narrative: prose story connecting data points
//
// The algorithm varies → Strategy Pattern is the correct prescription.
// =============================================================================

class FullReport {
    private final String title;
    private final String author;
    private final Map<String, List<String>> sections; // section name -> rows

    public FullReport(String title, String author, Map<String, List<String>> sections) {
        this.title    = title;
        this.author   = author;
        this.sections = sections;
    }
    public String getTitle()                          { return title; }
    public String getAuthor()                         { return author; }
    public Map<String, List<String>> getSections()    { return sections; }
    public int getTotalRows() {
        return sections.values().stream().mapToInt(List::size).sum();
    }
}

// ---------------------------------------------------------------------------
// v1  (Month 1) — Two summarization styles. CORRECT AS-IS. No pressure yet.
// ---------------------------------------------------------------------------
class ReportSummarizerV1 {

    public String summarize(FullReport report, String style) {
        if (style.equals("brief"))    return briefSummary(report);
        if (style.equals("detailed")) return detailedSummary(report);
        throw new IllegalArgumentException("Unknown style: " + style);
    }

    private String briefSummary(FullReport r) {
        return "Report: " + r.getTitle() + " | Sections: " + r.getSections().size();
    }

    private String detailedSummary(FullReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(r.getTitle()).append("\n");
        sb.append("Author: ").append(r.getAuthor()).append("\n");
        for (Map.Entry<String, List<String>> e : r.getSections().entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue().size()).append(" rows\n");
        }
        return sb.toString();
    }
}

// ---------------------------------------------------------------------------
// v2  (Month 9) — Five styles: executive, visual, narrative added.
//
// PAIN POINTS (behavior variation signals):
//   [!] Each new style is a genuinely different COMPUTATION, not just a
//       different serialization vocabulary (contrast with Bucket 1)
//   [!] briefSummary skips rows entirely; detailedSummary traverses them;
//       executiveSummary aggregates KPIs; visualSummary draws ASCII bars;
//       narrativeSummary connects data points in prose
//   [!] "Data scientists want to add a custom algorithm at runtime by
//       dataset size" — this is the canonical behavior variation signal
//   [!] Every new style requires opening ReportSummarizer and modifying it
//   [!] Unit-testing a single style requires instantiating the whole class
//
// Diagnosis: Behavior variation. Strategy Pattern is correct here.
// ---------------------------------------------------------------------------
class ReportSummarizerV2 {

    public String summarize(FullReport report, String style) {
        if (style.equals("brief"))      return briefSummary(report);
        if (style.equals("detailed"))   return detailedSummary(report);
        if (style.equals("executive"))  return executiveSummary(report);   // added month 4
        if (style.equals("visual"))     return visualSummary(report);      // added month 7
        if (style.equals("narrative"))  return narrativeSummary(report);   // added month 9
        throw new IllegalArgumentException("Unknown style: " + style);
    }

    private String briefSummary(FullReport r) {
        return "Report: " + r.getTitle() + " | Sections: " + r.getSections().size();
    }
    private String detailedSummary(FullReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(r.getTitle()).append("\n");
        for (Map.Entry<String, List<String>> e : r.getSections().entrySet())
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue().size()).append(" rows\n");
        return sb.toString();
    }
    private String executiveSummary(FullReport r) {
        // KPI aggregation — different computation from detailedSummary
        return "EXECUTIVE: " + r.getTitle() +
               " | Total rows: " + r.getTotalRows() +
               " | Sections: " + r.getSections().size() +
               " | Author: " + r.getAuthor();
    }
    private String visualSummary(FullReport r) {
        // ASCII bar chart — completely different algorithm
        StringBuilder sb = new StringBuilder("VISUAL SUMMARY: " + r.getTitle() + "\n");
        for (Map.Entry<String, List<String>> e : r.getSections().entrySet()) {
            String bar = "#".repeat(Math.min(e.getValue().size(), 20));
            sb.append(String.format("%-12s |%s| %d%n", e.getKey(), bar, e.getValue().size()));
        }
        return sb.toString();
    }
    private String narrativeSummary(FullReport r) {
        // Prose narrative — again, a different algorithm
        return "The report \"" + r.getTitle() + "\" by " + r.getAuthor() +
               " covers " + r.getSections().size() + " sections with " +
               r.getTotalRows() + " data points. " +
               "Key sections include: " +
               r.getSections().keySet().stream().limit(3).collect(Collectors.joining(", ")) + ".";
    }
}

// ---------------------------------------------------------------------------
// v3  (Refactored) — Strategy Pattern applied
//
// WHAT CHANGED:
//   - Introduced SummaryStrategy interface with a single summarize() method
//   - Each algorithm is now a standalone class (BriefSummary, DetailedSummary,
//     ExecutiveSummary, VisualSummary, NarrativeSummary)
//   - ReportSummarizerV3 accepts a SummaryStrategy via constructor (or setter)
//   - Zero if-else in the summarizer itself
//
// WHY Strategy (not Bucket 1 map, not Bucket 2 interface):
//   - The algorithms differ in HOW they compute, not just in which resource
//     they talk to or which vocabulary they use
//   - "Swap algorithm at runtime" is the canonical trigger
//   - Each strategy can be tested, benchmarked, and evolved independently
//   - A data scientist can provide a new NlpSummaryStrategy without touching
//     the ReportSummarizer class at all
// ---------------------------------------------------------------------------
interface SummaryStrategy {
    String summarize(FullReport report);
}

class BriefSummary implements SummaryStrategy {
    @Override
    public String summarize(FullReport r) {
        return "Report: " + r.getTitle() + " | Sections: " + r.getSections().size();
    }
}

class DetailedSummary implements SummaryStrategy {
    @Override
    public String summarize(FullReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(r.getTitle()).append("\n");
        sb.append("Author: ").append(r.getAuthor()).append("\n");
        for (Map.Entry<String, List<String>> e : r.getSections().entrySet())
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue().size()).append(" rows\n");
        return sb.toString();
    }
}

class ExecutiveSummary implements SummaryStrategy {
    @Override
    public String summarize(FullReport r) {
        return "EXECUTIVE: " + r.getTitle() +
               " | Total rows: " + r.getTotalRows() +
               " | Sections: " + r.getSections().size();
    }
}

class VisualSummary implements SummaryStrategy {
    @Override
    public String summarize(FullReport r) {
        StringBuilder sb = new StringBuilder("VISUAL: " + r.getTitle() + "\n");
        for (Map.Entry<String, List<String>> e : r.getSections().entrySet()) {
            String bar = "#".repeat(Math.min(e.getValue().size(), 20));
            sb.append(String.format("%-12s |%s| %d%n", e.getKey(), bar, e.getValue().size()));
        }
        return sb.toString();
    }
}

class NarrativeSummary implements SummaryStrategy {
    @Override
    public String summarize(FullReport r) {
        return "The report \"" + r.getTitle() + "\" by " + r.getAuthor() +
               " covers " + r.getSections().size() + " sections with " +
               r.getTotalRows() + " data points. Key sections include: " +
               r.getSections().keySet().stream().limit(3).collect(Collectors.joining(", ")) + ".";
    }
}

// New algorithm added with zero changes to existing code
class NlpSummary implements SummaryStrategy {
    @Override
    public String summarize(FullReport r) {
        return "[NLP] Entity extraction on " + r.getTotalRows() + " rows from \"" + r.getTitle() + "\"";
    }
}

class ReportSummarizerV3 {

    private SummaryStrategy strategy;

    public ReportSummarizerV3(SummaryStrategy strategy) {
        this.strategy = strategy;
    }

    // Swap strategy at runtime (e.g. based on dataset size)
    public void setStrategy(SummaryStrategy strategy) {
        this.strategy = strategy;
    }

    // No if-else — delegates entirely to the strategy
    public String summarize(FullReport report) {
        return strategy.summarize(report);
    }
}

// ---------------------------------------------------------------------------
// Demo — compiles and runs
// ---------------------------------------------------------------------------
public class bucket3_BehaviorVariation {
    public static void main(String[] args) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("Revenue",  Arrays.asList("Q1", "Q2", "Q3", "Q4"));
        sections.put("Expenses", Arrays.asList("Ops", "HR"));
        sections.put("Forecast", Arrays.asList("2025", "2026", "2027"));
        FullReport report = new FullReport("Annual Review", "Alice", sections);

        // v1 — two styles
        System.out.println("=== v1 (brief) ===");
        System.out.println(new ReportSummarizerV1().summarize(report, "brief"));

        // v2 — five styles, if-else pain
        System.out.println("\n=== v2 (visual) ===");
        System.out.println(new ReportSummarizerV2().summarize(report, "visual"));

        // v3 — strategy injected, swap at runtime
        ReportSummarizerV3 v3 = new ReportSummarizerV3(new BriefSummary());
        System.out.println("\n=== v3 (brief) ===");
        System.out.println(v3.summarize(report));

        v3.setStrategy(new VisualSummary());
        System.out.println("\n=== v3 (visual) ===");
        System.out.println(v3.summarize(report));

        // Runtime swap based on dataset size — canonical Strategy trigger
        int rowCount = report.getTotalRows();
        v3.setStrategy(rowCount > 100 ? new NlpSummary() : new ExecutiveSummary());
        System.out.println("\n=== v3 (auto-selected by size=" + rowCount + ") ===");
        System.out.println(v3.summarize(report));
    }
}
