import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Pressure: Orbit keeps a run's progress only in the model's context. When the worker dies mid-run,
 * the stub model replans from step one and the gateway sends every step again under a fresh request id.
 * The "model" is a deterministic scripted stub, not a real model.
 */
public class Main {
    record Call(String tool, String order, int amount) {
        String effect() { return tool + ":" + order + ":" + amount; }
    }
    record Step(String id, Call call) {}

    static int seq = 0, tick = 0, worker = 1, counter = 0, deduped = 0;
    static final List<String> applied = new ArrayList<>();
    static final Set<String> seenIds = new LinkedHashSet<>();
    static final Set<String> acked = new LinkedHashSet<>();
    static final Map<String, List<String>> idsByStep = new LinkedHashMap<>();
    static final Map<String, Integer> firstTick = new LinkedHashMap<>();

    static void section(String title) {
        System.out.println("-- " + title + " --");
    }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    // Multiset difference: the entries of a that b does not account for.
    static List<String> excess(List<String> a, List<String> b) {
        List<String> rest = new ArrayList<>(a);
        b.forEach(rest::remove);
        return rest;
    }

    static List<String> intended(List<Step> steps) {
        return steps.stream().map(s -> s.call().effect()).toList();
    }

    static boolean exact(List<String> want) {
        return excess(applied, want).isEmpty() && excess(want, applied).isEmpty();
    }

    static int count(List<String> effects, String tool) {
        return (int) effects.stream().filter(e -> e.startsWith(tool + ":")).count();
    }

    static int dollars(List<String> effects) {
        return effects.stream().filter(e -> e.startsWith("refund:"))
            .mapToInt(e -> Integer.parseInt(e.substring(e.lastIndexOf(':') + 1))).sum();
    }

    // The gateway mints a fresh request id per attempt; Meridian dedupes on request ids, but none ever repeats.
    // DUPLICATED is the harness's judgment from its own ledger; the gateway cannot see it.
    static void send(String run, Step s, boolean acks) {
        Call c = s.call();
        boolean refund = c.tool().equals("refund");
        String step = run + "/" + s.id(), req = "w" + worker + "-" + (++counter), dst = refund ? "meridian" : "mailer";
        String what = refund ? "refund order " + c.order() + " $" + c.amount() : "email order " + c.order();
        String msg = "t=" + (++tick) + " " + s.id() + " " + what + " req=" + req;
        idsByStep.computeIfAbsent(step, k -> new ArrayList<>()).add(req);
        if (!seenIds.add(req)) {
            deduped++;
            event("gateway", dst, msg, "DEDUPED");
            return;
        }
        applied.add(c.effect());
        Integer first = firstTick.putIfAbsent(step, tick);
        event("gateway", dst, msg, first == null ? (refund ? "EXECUTED" : "SENT")
            : "DUPLICATED (first " + (refund ? "applied" : "sent") + " at t=" + first + ")");
        if (acks) {
            acked.add(step);
        }
    }

    public static void main(String[] args) {
        List<Step> quiet = List.of(new Step("s1", new Call("refund", "5101", 20)),
            new Step("s2", new Call("refund", "5101", 20)), new Step("s3", new Call("email", "5101", 0)));
        List<Step> plan = List.of(new Step("s1", new Call("refund", "5102", 65)),
            new Step("s2", new Call("email", "5102", 0)), new Step("s3", new Call("refund", "5103", 30)),
            new Step("s4", new Call("email", "5103", 0)));

        section("Quiet run run-0416");
        event("orchestrator", "ops-agent", "run-0416 starts, plan and progress held only in the model context", "STARTED");
        quiet.forEach(s -> send("run-0416", s, true));
        boolean legit = exact(intended(quiet)) && deduped == 0;

        section("Crash mid-run run-0417");
        event("orchestrator", "ops-agent", "run-0417 starts, plan and progress held only in the model context", "STARTED");
        plan.subList(0, 3).forEach(s -> send("run-0417", s, !s.id().equals("s3")));
        event("orchestrator", "ops-agent", "t=" + (++tick) + " worker 1 dies before s3 is acknowledged, "
            + "run-0417 reschedules on worker 2 with an empty context", "INFO");
        List<String> completed = acked.stream().filter(k -> k.startsWith("run-0417/")).toList();

        section("Restart and replay");
        worker = 2;
        counter = 0;
        event("ops-agent", "orchestrator", "context empty, no record of s1 to s3, replans from step one", "INFO");
        plan.forEach(s -> send("run-0417", s, true));

        List<String> want = new ArrayList<>(intended(quiet));
        want.addAll(intended(plan));
        List<String> resent = idsByStep.keySet().stream().filter(k -> idsByStep.get(k).size() > 1).toList();
        long newId = resent.stream().filter(k -> new LinkedHashSet<>(idsByStep.get(k)).size() > 1).count();
        long again = completed.stream().filter(resent::contains).count();
        System.out.println("METRIC refunds_applied = " + count(applied, "refund"));
        System.out.println("METRIC dollars_paid = " + dollars(applied));
        System.out.println("METRIC dollars_intended = " + dollars(want));
        System.out.println("METRIC emails_sent = " + count(applied, "email"));
        System.out.println("METRIC requests_deduped_by_meridian = " + deduped);
        check("legit-uninterrupted-run-works", legit, "run-0416: two $20 credits and one email, each applied once",
            "run-0416 did not apply each effect once");
        check("effect-applied-exactly-once", exact(want), "every refund and email applied once",
            count(applied, "refund") + " refunds, $" + dollars(applied) + " paid vs $" + dollars(want) + " intended; "
            + count(applied, "email") + " emails vs " + count(want, "email") + " intended");
        check("resume-skips-completed-steps", again == 0, "no completed step was sent again",
            again + " of " + completed.size() + " completed steps sent again after the restart");
        check("idempotency-key-stable-across-retries", newId == 0, "re-sent steps kept their request id",
            newId + " of " + resent.size() + " re-sent steps went out under a new request id");
    }
}
