# Evolution: Visitor Pattern — Financial Report Renderer

Domain: **Financial Report Renderer** (BalanceSheet, IncomeStatement, CashFlowStatement)

## The Forcing Function

You have **3 stable report types** and **growing rendering operations** (PDF, HTML, Excel, CSV, Slack summary).
Every time a new operation is needed the same question appears: where does the code go?

## Progression

| File | What it shows | The pain |
|------|--------------|----------|
| `v1_ScatteredMethods.java` | 3 report types × 2 operations = 6 methods living inside the domain objects | Adding `renderAsExcel()` means touching all 3 classes |
| `v2_PartialRefactor.java` | Operations extracted into `FinancialReportRenderer`, but it uses `instanceof` chains | Adding a new operation = another `instanceof` chain; renderer never closes |
| `v3_VisitorApplied.java` | `ReportVisitor` interface + `accept(visitor)` on each report type | Adding `ExcelExportVisitor` = 1 new class, zero edits to existing code |

## When Visitor Wins

- Object type set is **stable** (you have exactly 3 report types and that won't change)
- Operations **grow** over time (PDF, HTML, Excel, CSV, Slack, audit trail...)
- You want each operation to be independently testable and deployable

## When Visitor Loses

- Report types change frequently (every new type requires editing every visitor)
- There are only 1–2 operations that will never grow

## Run it

```bash
cd evolution/
javac v1_ScatteredMethods.java && java v1_ScatteredMethods
javac v2_PartialRefactor.java  && java v2_PartialRefactor
javac v3_VisitorApplied.java   && java v3_VisitorApplied
```

Or compile all at once:

```bash
javac *.java && java v3_VisitorApplied
```

## Key Insight

The `accept(visitor)` call is **double dispatch** — the runtime picks the right
`visit()` overload based on the concrete report type, not an `instanceof` check.
The compiler enforces completeness: if you add a new report type, every existing
visitor fails to compile until you add the missing `visit()` method.
