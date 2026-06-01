# Job Scheduler — Command Pattern with Deferred Execution

## What This Demonstrates

`EmailJob`, `ReportGenerationJob`, and `DataSyncJob` as scheduled commands
with automatic retry on failure (up to 3 attempts). `JobScheduler` treats all
job types uniformly via the `Job` interface — it never branches on job type.
Adding a new job type requires one new class; the scheduler never changes.

**Pressure: Behavior Encapsulation** — operations must be deferred, retried,
and tracked independently of the scheduler that drives them.

## Class Diagram

```
<<interface>>
Job  (extends Command concept)
+ execute(): void  throws Exception
+ undo(): void
+ getJobId(): String
+ getScheduledAt(): Instant
+ getRetryCount(): int
+ withIncrementedRetry(): Job    ← returns NEW instance, never mutates
+ describe(): String
        △
        |
   ────────────────────────────────────────
   |                    |                 |
EmailJob        ReportGenerationJob    DataSyncJob
- jobId          - jobId                - jobId
- recipient      - reportType           - sourceSystem
- subject        - bucketName           - targetSystem
- scheduledAt    - scheduledAt          - scheduledAt
- retryCount     - retryCount           - retryCount

JobScheduler                              [invoker]
- queue: PriorityQueue<Job>    (min-heap by scheduledAt)
- completedJobs: List<Job>
- failedJobs: List<Job>
+ schedule(job): void
+ runDue(): void               → drains due jobs, calls execute()
+ printSummary(): void
  private runWithRetry(job)    → retry up to MAX_RETRIES=3
```

## Sequence / Flow

```
Client
  │
  ├─ scheduler.schedule(new EmailJob("EMAIL-001", ..., now))
  ├─ scheduler.schedule(new ReportGenerationJob("REPORT-001", ..., now))
  ├─ scheduler.schedule(new EmailJob("EMAIL-FAIL", ..., now))  ← designed to fail once
  └─ scheduler.schedule(new ReportGenerationJob("REPORT-002", ..., now+3600s))
  │
  └─ scheduler.runDue()
          │
          ├─ EmailJob "EMAIL-001" → execute() → succeeds
          │       └─ completedJobs.add(job)
          │
          ├─ ReportGenerationJob "REPORT-001" → execute() → succeeds
          │
          ├─ EmailJob "EMAIL-FAIL" → execute() → throws (SMTP timeout)
          │       └─ retryCount(0) < MAX_RETRIES(3)
          │       └─ job.withIncrementedRetry()  → new EmailJob(retryCount=1)
          │       └─ re-enqueue and run immediately
          │       └─ EmailJob "EMAIL-FAIL" → execute() → succeeds (attempt 2)
          │
          └─ "REPORT-002" scheduledAt is in the future → stays in queue
```

## Design Decisions

- **`withIncrementedRetry()` returns a new instance** — jobs are immutable.
  Safe to enqueue multiple times without corrupting shared state. The scheduler
  never mutates a job object; it always works with a fresh copy per attempt.
- **`JobScheduler` knows nothing about job types** — it calls `execute()` on
  a `Job` reference. The actual work (SMTP, S3 upload, BigQuery sync) lives
  entirely in the concrete job class.
- **Min-heap ordered by `scheduledAt`** — `runDue()` processes jobs in
  chronological order without scanning the full queue.
- **`undo()` is defined but semantically limited** — emails cannot be recalled;
  `EmailJob.undo()` logs a warning rather than silently no-oping. The interface
  forces every job to make a deliberate choice about reversibility.
- **`MAX_RETRIES = 3` is a scheduler-level policy** — jobs do not hardcode
  their own retry limit. The scheduler controls the policy uniformly across
  all job types.

## How to Run

```bash
cd volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/job-scheduler
javac *.java && java Main
```

Expected output (timestamps vary):

```
=== Scheduling jobs ===
  [Scheduler] Scheduled  EmailJob[EMAIL-001] -> alice@example.com "Order Confirmation #1001"
  [Scheduler] Scheduled  ReportJob[REPORT-001] ...
  [Scheduler] Scheduled  DataSyncJob[SYNC-001] ...
  [Scheduler] Scheduled  EmailJob[EMAIL-FAIL] ...
  [Scheduler] Scheduled  ReportJob[REPORT-002] ...   ← 1 hour from now

=== Running due jobs ===
  [EmailJob EMAIL-001] Sent "Order Confirmation #1001" to alice@example.com (attempt 1)
  [Scheduler] FAILED EmailJob[EMAIL-FAIL] — SMTP connection timed out
  [Scheduler] Retrying EMAIL-FAIL (attempt 2/4)
  [EmailJob EMAIL-FAIL] Sent "Password Reset" to bob@example.com (attempt 2)

  [Scheduler] Summary:
    completed: 4, failed: 0, pending: 1

Note: REPORT-002 is still pending — scheduled 1 hour from now.
```

## When to Apply

- Background operations that must be deferred, retried, and tracked
  independently without the scheduler knowing what each job does.
- New job types arrive regularly — adding one class per type beats growing
  a switch in the scheduler.

## When NOT to Apply

- Synchronous request/response pipelines with no deferred execution — the
  scheduling overhead solves a problem that does not exist there.
