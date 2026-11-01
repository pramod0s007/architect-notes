import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

// Principle: persist the plan, write STARTED before and DONE after every effect, and resume from that log.
// A request key is run id + step id + a digest of the canonical action, never the free text.
// Meridian applies a key once; the demo's mailer ignores keys. The "model" is a deterministic scripted stub.
public class Main {
    record Call(String tool, String order, int amount) { boolean refund() { return tool.equals("refund"); } }
    record Step(String id, Call call, String text) {}
    record Rec(String run, String step, String kind, String key) {}

    static int seq = 0, tick = 0;
    static boolean trace = false;
    static final Map<String, List<Step>> plans = new LinkedHashMap<>();    // durable: saved before the first effect
    static final List<Rec> runlog = new ArrayList<>();                     // durable: STARTED and DONE records
    static final Map<String, Integer> keysApplied = new LinkedHashMap<>(); // Meridian: key -> tick of first apply
    static final List<String> keysSent = new ArrayList<>();                // every key that went on the wire
    static final List<Call> applied = new ArrayList<>();                   // every effect that really happened

    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }
    static void check(String name, boolean pass, String facts) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + facts + ")");
    }
    static List<String> sorted(List<Call> calls) { return calls.stream().map(Call::toString).sorted().toList(); }
    static String tally(List<Call> calls) {
        long refunds = calls.stream().filter(Call::refund).count();
        return "refunds " + refunds + " ($" + calls.stream().mapToInt(Call::amount).sum() + "), emails " + (calls.size() - refunds);
    }
    static List<String> keysOf(String run, String step) {
        return keysSent.stream().filter(k -> k.startsWith(run + "/" + step + "/")).toList();
    }
    static Optional<Rec> find(String run, String step, String kind) {
        return runlog.stream().filter(r -> r.run().equals(run) && r.step().equals(step) && r.kind().equals(kind)).findFirst();
    }

    // Canonical action, format v1: tool, order and amount. The free text is deliberately left out.
    static String key(String run, Step s) {
        String canonical = "v1|" + s.call().tool() + "|order=" + s.call().order() + "|amount=" + s.call().amount();
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return run + "/" + s.id() + "/" + HexFormat.of().formatHex(d, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // Meridian applies a key once. The mailer ignores keys, so only the run log protects an email.
    static void send(Step s, String key, String tag) {
        Call c = s.call();
        String what = c.refund() ? "refund order " + c.order() + " $" + c.amount() : "email order " + c.order();
        String msg = "t=" + (++tick) + " " + s.id() + " " + tag + what + " key=" + key;
        keysSent.add(key);
        Integer first = c.refund() ? keysApplied.putIfAbsent(key, tick) : null;
        if (first == null) {
            applied.add(c);
        }
        event("gateway", c.refund() ? "meridian" : "mailer", msg,
            first != null ? "DEDUPED (key applied at t=" + first + ")" : c.refund() ? "EXECUTED" : "SENT");
    }

    static void write(String run, String step, String kind, String key) {
        runlog.add(new Rec(run, step, kind, key));
        if (trace) {
            event("orchestrator", "runlog", step + " " + kind + (kind.equals("STARTED") ? " key=" + key : ""), "RECORDED");
        }
    }

    // One worker pass. The first run and the resume are the same loop: the log decides what is left and which key to use.
    static void work(String run, String diesAt) {
        for (Step s : plans.get(run).stream().filter(p -> find(run, p.id(), "DONE").isEmpty()).toList()) {
            Optional<Rec> logged = find(run, s.id(), "STARTED");
            String key = logged.isPresent() ? logged.get().key() : key(run, s);
            if (logged.isEmpty()) {
                write(run, s.id(), "STARTED", key);
            }
            send(s, key, logged.isPresent() ? "re-issue " : "");
            if (s.id().equals(diesAt)) {
                event("orchestrator", "runlog", "t=" + (++tick) + " worker 1 dies after " + s.id()
                    + " is applied, before its DONE record", "INFO");
                return;
            }
            write(run, s.id(), "DONE", key);
        }
    }

    static void start(String run, List<Step> plan, String diesAt) {
        plans.put(run, plan);
        event("orchestrator", "runlog", "plan of " + run + " persisted, " + plan.size() + " steps, before the first effect", "RECORDED");
        work(run, diesAt);
    }

    public static void main(String[] args) {
        String credit = "Refund $20 to order 5101, the parcel arrived late", tell = "Tell the customer on order ";
        List<Step> quiet = List.of(new Step("s1", new Call("refund", "5101", 20), credit),
            new Step("s2", new Call("refund", "5101", 20), credit),
            new Step("s3", new Call("email", "5101", 0), tell + "5101 that the refund was issued"));
        List<Step> plan = List.of(new Step("s1", new Call("refund", "5102", 65), "Refund $65 to order 5102, item arrived damaged"),
            new Step("s2", new Call("email", "5102", 0), tell + "5102 that the refund was issued"),
            new Step("s3", new Call("refund", "5103", 30), "Refund $30 to order 5103, wrong size shipped"),
            new Step("s4", new Call("email", "5103", 0), tell + "5103 that the refund was issued"));
        List<Call> quietWant = quiet.stream().map(Step::call).toList(), want = new ArrayList<>(quietWant);
        plan.forEach(s -> want.add(s.call()));

        section("Quiet run run-0416");
        start("run-0416", quiet, null);
        boolean legit = sorted(applied).equals(sorted(quietWant)) && keysSent.size() == applied.size();
        String quietFacts = "run-0416 applied " + tally(applied) + ", deduped " + (keysSent.size() - applied.size());

        trace = true;
        section("Crash mid-run run-0417");
        start("run-0417", plan, "s3");
        List<String> doneBefore = runlog.stream().filter(r -> r.run().equals("run-0417") && r.kind().equals("DONE")).map(Rec::step).toList();
        List<String> open = runlog.stream().filter(r -> r.kind().equals("STARTED") && find(r.run(), r.step(), "DONE").isEmpty())
            .map(Rec::step).toList();

        section("Resume from the log");
        event("orchestrator", "runlog", "worker 2 reads the run-0417 log: " + String.join(", ", doneBefore) + " DONE, "
            + String.join(", ", open) + " STARTED without DONE; plan restored, model not asked to replan", "INFO");
        work("run-0417", null);

        section("A late duplicate from the model");
        Step again = new Step("s3", plan.get(2).call(), "Credit $30 back on order 5103, wrong size shipped");
        event("ops-agent", "gateway", "s3 arrives again, reworded \"" + again.text() + "\"", null);
        send(again, key("run-0417", again), "");

        List<String> s3 = keysOf("run-0417", "s3"); // in order: worker 1, the re-issue, the model's late copy
        String loggedKey = find("run-0417", "s3", "STARTED").orElseThrow().key();
        long deduped = keysSent.size() - applied.size();
        long doneRequests = doneBefore.stream().mapToLong(id -> keysOf("run-0417", id).size()).sum();
        System.out.println("METRIC refunds_applied = " + applied.stream().filter(Call::refund).count());
        System.out.println("METRIC emails_sent = " + applied.stream().filter(c -> !c.refund()).count());
        System.out.println("METRIC meridian_requests = " + (keysApplied.size() + deduped));
        System.out.println("METRIC meridian_deduped = " + deduped);
        System.out.println("METRIC runlog_records = " + runlog.size());
        check("effect-applied-exactly-once", sorted(applied).equals(sorted(want)),
            "applied " + tally(applied) + "; intended " + tally(want));
        check("resume-skips-completed-steps", doneRequests == doneBefore.size(),
            "done before the crash: " + String.join(", ", doneBefore) + "; requests sent for those steps: " + doneRequests);
        check("idempotency-key-stable-across-retries", s3.stream().distinct().count() == 1,
            s3.size() + " requests for run-0417 s3, distinct keys: " + s3.stream().distinct().count());
        check("in-flight-step-reissued-with-same-key", s3.get(1).equals(loggedKey),
            "re-issue sent " + s3.get(1) + "; the log holds " + loggedKey);
        check("legit-uninterrupted-run-works", legit, quietFacts);
    }
}
