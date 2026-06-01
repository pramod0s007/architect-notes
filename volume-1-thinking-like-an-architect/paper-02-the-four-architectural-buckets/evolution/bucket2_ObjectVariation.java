import java.util.*;

// =============================================================================
// BUCKET 2: OBJECT VARIATION — Report Persistence
// =============================================================================
// Symptom : if-else chain growing as new storage targets are added
// Pressure : OBJECT variation — the STORAGE ENGINE changes, workflow is stable
// Solution : Interface + Composition (inject the storage object) — NOT Strategy Pattern
//
// Key insight: The workflow is fixed:
//   validate → serialize → persist → log
// Only the "persist" step changes. The object being swapped is a collaborator
// (a storage engine), not an algorithm. This is composition/DI, not Strategy.
//
// Strategy Pattern is wrong here because:
//   - Strategy encapsulates a COMPUTATION (algorithm)
//   - Here we are encapsulating a RESOURCE (a connection to a storage system)
//   - The distinction matters: object variation = inject a new collaborator;
//     behavior variation = inject a new computation
// =============================================================================

// ---------------------------------------------------------------------------
// Shared domain objects
// ---------------------------------------------------------------------------
class ReportRecord {
    private final String id;
    private final String content;
    public ReportRecord(String id, String content) { this.id = id; this.content = content; }
    public String getId()      { return id; }
    public String getContent() { return content; }
}

// ---------------------------------------------------------------------------
// v1  (Month 1) — Save to MySQL only. CORRECT AS-IS. No pressure yet.
//     The workflow (validate → serialize → persist → log) is clear.
// ---------------------------------------------------------------------------
class ReportPersistenceV1 {

    public void save(ReportRecord report) {
        validate(report);
        String serialized = serialize(report);
        saveMysql(serialized, report.getId());   // only one storage target
        log("Saved report " + report.getId());
    }

    private void validate(ReportRecord r) {
        if (r.getId() == null || r.getId().isEmpty())
            throw new IllegalArgumentException("Report id must not be empty");
    }
    private String serialize(ReportRecord r) {
        return "id=" + r.getId() + ";content=" + r.getContent();
    }
    private void saveMysql(String data, String id) {
        System.out.println("[MySQL] INSERT id=" + id + " data=" + data);
    }
    private void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}

// ---------------------------------------------------------------------------
// v2  (Month 8) — MongoDB and S3 added. Three targets and growing.
//
// PAIN POINTS (object variation signals):
//   [!] validate() has not changed — it is workflow logic, not storage logic
//   [!] serialize() has not changed — same story
//   [!] log() has not changed
//   [!] Only the saveMysql/saveMongo/saveS3 calls differ — and they all
//       do exactly the same thing: "persist this blob somewhere"
//   [!] The workflow shape (validate → serialize → persist → log) is stable
//   [!] Each storage backend is a RESOURCE with its own connection — not
//       a computation you want to swap at runtime by strategy
//
// Diagnosis: Object variation. The "persist" collaborator changes.
// Fix: extract a ReportStore interface, inject it. The workflow stays put.
// ---------------------------------------------------------------------------
class ReportPersistenceV2 {

    public void save(ReportRecord report, String target) {
        validate(report);
        String serialized = serialize(report);

        // [!] This block is the only thing that changes — it is not an algorithm,
        //     it is a choice of which external resource to talk to
        if (target.equals("mysql"))   saveMysql(serialized, report.getId());
        else if (target.equals("mongo"))  saveMongo(serialized, report.getId());
        else if (target.equals("s3"))     saveS3(serialized, report.getId());  // added month 6
        else throw new IllegalArgumentException("Unknown target: " + target);

        log("Saved report " + report.getId() + " to " + target);
    }

    private void validate(ReportRecord r) {
        if (r.getId() == null || r.getId().isEmpty())
            throw new IllegalArgumentException("Report id must not be empty");
    }
    private String serialize(ReportRecord r) {
        return "id=" + r.getId() + ";content=" + r.getContent();
    }
    private void saveMysql(String data, String id) {
        System.out.println("[MySQL] INSERT id=" + id);
    }
    private void saveMongo(String data, String id) {
        System.out.println("[MongoDB] insertOne id=" + id);
    }
    private void saveS3(String data, String id) {
        System.out.println("[S3] putObject key=" + id);
    }
    private void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}

// ---------------------------------------------------------------------------
// v3  (Refactored) — Interface + Composition. The workflow is untouched.
//
// WHAT CHANGED:
//   - Extracted ReportStore interface (the "persist" step is now a seam)
//   - Three concrete stores: MysqlReportStore, MongoReportStore, S3ReportStore
//   - ReportPersistenceV3 receives its store via constructor (DI)
//   - The validate → serialize → persist → log workflow is unchanged
//
// WHY NOT Strategy Pattern here:
//   - Strategy is about swapping a COMPUTATION (e.g. a sorting algorithm).
//   - ReportStore encapsulates a RESOURCE (a DB connection / HTTP client).
//   - Naming it "ReportStorageStrategy" would mislead readers into thinking
//     the algorithm varies. The correct name is "ReportStore" — a collaborator.
//   - If we also needed to vary HOW we serialize (e.g. binary vs text vs columnar),
//     THAT would be behavior variation and would deserve a Strategy.
// ---------------------------------------------------------------------------
interface ReportStore {
    // Contract: persist serialized report data under the given id
    void persist(String serializedData, String id);
}

class MysqlReportStore implements ReportStore {
    @Override
    public void persist(String data, String id) {
        System.out.println("[MySQL] INSERT id=" + id + " data=" + data);
    }
}

class MongoReportStore implements ReportStore {
    @Override
    public void persist(String data, String id) {
        System.out.println("[MongoDB] insertOne id=" + id);
    }
}

class S3ReportStore implements ReportStore {
    private final String bucketName;
    public S3ReportStore(String bucketName) { this.bucketName = bucketName; }
    @Override
    public void persist(String data, String id) {
        System.out.println("[S3:" + bucketName + "] putObject key=" + id);
    }
}

// Adding a new store (e.g. Redis) = new class, zero changes to this workflow.
class RedisReportStore implements ReportStore {
    @Override
    public void persist(String data, String id) {
        System.out.println("[Redis] SET " + id + " -> " + data);
    }
}

class ReportPersistenceV3 {

    private final ReportStore store;  // injected — the only thing that varies

    public ReportPersistenceV3(ReportStore store) {
        this.store = store;
    }

    // Workflow is IDENTICAL to v1 and v2 — only the collaborator is swapped
    public void save(ReportRecord report) {
        validate(report);
        String serialized = serialize(report);
        store.persist(serialized, report.getId());   // seam: no if-else
        log("Saved report " + report.getId());
    }

    private void validate(ReportRecord r) {
        if (r.getId() == null || r.getId().isEmpty())
            throw new IllegalArgumentException("Report id must not be empty");
    }
    private String serialize(ReportRecord r) {
        return "id=" + r.getId() + ";content=" + r.getContent();
    }
    private void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}

// ---------------------------------------------------------------------------
// Demo — compiles and runs
// ---------------------------------------------------------------------------
public class bucket2_ObjectVariation {
    public static void main(String[] args) {
        ReportRecord report = new ReportRecord("RPT-001", "Q1 Revenue report body");

        // v1 — single storage target
        System.out.println("=== v1 ===");
        new ReportPersistenceV1().save(report);

        // v2 — if-else pain across three targets
        System.out.println("\n=== v2 ===");
        ReportPersistenceV2 v2 = new ReportPersistenceV2();
        v2.save(report, "mysql");
        v2.save(report, "s3");

        // v3 — inject the store, workflow unchanged
        System.out.println("\n=== v3 (MySQL) ===");
        new ReportPersistenceV3(new MysqlReportStore()).save(report);

        System.out.println("\n=== v3 (S3) ===");
        new ReportPersistenceV3(new S3ReportStore("reports-bucket")).save(report);

        System.out.println("\n=== v3 (Redis — new store, zero workflow changes) ===");
        new ReportPersistenceV3(new RedisReportStore()).save(report);
    }
}
