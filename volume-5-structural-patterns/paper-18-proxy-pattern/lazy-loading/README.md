# Lazy Report — Proxy Pattern (Virtual/Lazy Proxy)

## What This Demonstrates

Virtual Proxy Pattern applied to financial report loading. `LazyReportProxy`
implements the `Report` interface and defers construction of the real
`FinancialReport` until the first call to `getPdfContent()` or `getMetadata()`.
`getTitle()` returns immediately from data stored in the proxy itself. Once the
real report is loaded, subsequent calls reuse it. Thread safety is achieved via
double-checked locking with a `volatile` field.

**Pressure: Deferred expensive creation** — a dashboard lists 40 financial
reports by title. Each `FinancialReport` requires a 500ms database query and
PDF rendering pass. Without lazy loading, opening the dashboard triggered 40
sequential loads — 20 seconds of startup time for a page where users typically
open 1–2 reports. `LazyReportProxy` reduces dashboard load time to near-zero
and shifts the 500ms cost to the moment a specific report is opened.

## Class Diagram

```
<<interface>>
Report
+ getTitle(): String
+ getPdfContent(): byte[]
+ getMetadata(): Map<String, String>
        △
        |
   ──────────────────────────────────────────────────
   |                                                 |
FinancialReport (real subject)              LazyReportProxy (proxy)
- reportId: String                          - reportId: String
- title: String                             - title: String
- pdfContent: byte[]                        - volatile realReport: FinancialReport
- metadata: Map<String,String>              ─────────────────────────────────────
constructor(reportId, title):               getTitle()     → return title (instant)
  Thread.sleep(500ms)  ← expensive          getPdfContent() → getRealReport().getPdfContent()
  generate PDF content                      getMetadata()   → getRealReport().getMetadata()
  build metadata                            isLoaded()      → realReport != null
                                            ─────────────────────────────────────
                                            getRealReport():
                                              if realReport == null:        ← first check
                                                synchronized(this):
                                                  if realReport == null:    ← second check
                                                    realReport = new FinancialReport(...)

ReportCache
- reports: Map<String, LazyReportProxy>
+ register(id, title): void  → new LazyReportProxy(id, title)
+ getReport(id): Report
+ printReportList(): void    → shows id, title, and (loaded)/(not loaded)
```

## Sequence Diagram

```
Client                    LazyReportProxy          FinancialReport
  │                             │                        │
  │ new LazyReportProxy(id,title)                        │
  │────────────────────────────>│ (instant — no DB call) │
  │                             │                        │
  │ proxy.getTitle()            │                        │
  │────────────────────────────>│ return title           │
  │<────────────────────────────│ (instant)              │
  │                             │                        │
  │ proxy.getPdfContent()       │                        │
  │────────────────────────────>│ realReport == null     │
  │                             │──────────────────────> │ new FinancialReport()
  │                             │                        │   Thread.sleep(500ms)
  │                             │                        │   generate PDF
  │                             │<────────────────────── │ return FinancialReport
  │                             │ realReport = loaded    │
  │<────────────────────────────│ return pdfContent      │
  │                             │                        │
  │ proxy.getPdfContent()       │                        │
  │────────────────────────────>│ realReport != null     │
  │<────────────────────────────│ return instantly       │
```

## Double-Checked Locking — Plain English

The proxy uses `volatile` plus a two-step null check to initialize `realReport`
safely under concurrent access:

```java
private volatile FinancialReport realReport;  // volatile is essential

private FinancialReport getRealReport() {
    if (realReport == null) {           // first check — no lock (fast path)
        synchronized (this) {
            if (realReport == null) {   // second check — under lock
                realReport = new FinancialReport(reportId, title);
            }
        }
    }
    return realReport;
}
```

**Why two checks?**

- The first `if (realReport == null)` outside `synchronized` is the fast path.
  Once `realReport` is assigned, all threads take this branch and return
  immediately without acquiring the lock.
- Without the lock, two threads could both pass the first check simultaneously
  and both try to create `FinancialReport`. The `synchronized` block ensures only
  one thread does the actual construction.
- The second `if (realReport == null)` inside `synchronized` handles the race:
  the second thread to reach the lock finds `realReport` already assigned and
  skips construction.

**Why `volatile`?**

Without `volatile`, the Java memory model allows the JIT compiler to reorder
the internal steps of `new FinancialReport(...)`. Another thread could see a
non-null `realReport` reference that points to a partially constructed object
(fields not yet written). `volatile` on the field guarantees that the reference
only becomes visible to other threads after the object is fully constructed.

## Design Decisions

- **`getTitle()` returns from proxy fields, never touching `realReport`** — title
  is provided at proxy construction time. Listing 40 report titles is free; no
  `FinancialReport` objects are created. The proxy is not just a lazy initializer
  — it actively serves cheap data without delegating at all.
- **`isLoaded()` method on the proxy** — the `ReportCache.printReportList()` uses
  this to display `(loaded)` or `(not loaded)` next to each report title,
  allowing the demo to show which reports were accessed and which were not.
- **`FinancialReport` constructor simulates 500ms with `Thread.sleep`** — this
  makes the lazy loading benefit tangible and measurable in the demo output.
  In production this represents actual I/O time (DB query, PDF renderer).

## How to Run

```bash
cd volume-5-structural-patterns/paper-18-proxy-pattern/lazy-loading
javac *.java && java Main
```

Expected output (abbreviated):

```
=== Registering Reports (no load yet) ===
  [LazyReportProxy] Created proxy for 'Q1 2024 Financial Summary' (not loaded yet)
  [LazyReportProxy] Created proxy for 'Q2 2024 Financial Summary' (not loaded yet)
  ...

=== Reading Titles Directly (still cheap) ===
  Title: Q1 2024 Financial Summary
  Title: Q2 2024 Financial Summary
  ...

=== Requesting PDF for RPT-002 (triggers lazy load) ===
  [LazyReportProxy] Triggering lazy load for 'Q2 2024 Financial Summary'
  [FinancialReport] Loading report RPT-002 (500ms)...
  PDF loaded in ~500ms, size=... bytes

=== Second Access to RPT-002 (already loaded) ===
  Retrieved in 0ms (no reload)

=== Cache State After Accessing Only RPT-002 ===
  [RPT-001] Q1 2024 Financial Summary   (not loaded)
  [RPT-002] Q2 2024 Financial Summary   (loaded)
  [RPT-003] Annual 2023 Audited Report  (not loaded)
  [RPT-004] Board Meeting Deck          (not loaded)
```

## When to Apply

- An object's construction is expensive (I/O, computation) and it may not be
  accessed at all during a session.
- Cheap metadata (title, id, summary) should be immediately available for display
  while expensive content (PDF, large payload) is deferred.
- Multiple threads may access the same proxy concurrently.

## When NOT to Apply

- The object is always accessed — lazy initialization adds synchronization
  overhead with no savings if every proxy triggers a load immediately.
- Construction is fast (no I/O) — the complexity of double-checked locking is
  not justified for a cheap object.
