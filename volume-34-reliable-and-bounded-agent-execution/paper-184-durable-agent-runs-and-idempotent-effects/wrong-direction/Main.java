import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Wrong direction: the prompt tells the model to look for an action's description in the history and skip it
 * when found. The stub follows the rule literally and, after a restart, rewords two of its descriptions.
 * The "model" is a deterministic scripted stub, not a real model.
 */
public class Main {
    record Call(String tool, String order, int amount) {
        String effect() { return tool + ":" + order + ":" + amount; }
    }
    record Step(String id, Call call, String text) {}

    static int seq = 0, tick = 0, right = 0, wrong = 0, missed = 0;
    static final List<String> applied = new ArrayList<>();
    static final Set<String> history = new LinkedHashSet<>();
    static final Set<String> acked = new LinkedHashSet<>();
    static final Map<String, List<Integer>> ran = new LinkedHashMap<>();
    static final Map<String, List<String>> wordings = new LinkedHashMap<>();

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

    static String norm(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    // The rule reads only the wording. Whether a skip or a send was right is the harness's judgment:
    // a step is a repeat when its effect already ran. DEDUPED, LOST and DUPLICATED are stamped from that.
    static void work(String run, Step s, String text, boolean acks) {
        boolean refund = s.call().tool().equals("refund");
        String step = run + "/" + s.id(), head = "t=" + (++tick) + " " + s.id() + " \"" + text + "\"";
        boolean seen = history.contains(norm(text)), repeat = ran.containsKey(step);
        wordings.computeIfAbsent(step, k -> new ArrayList<>()).add(norm(text));
        if (seen) {
            right += repeat ? 1 : 0;
            wrong += repeat ? 0 : 1;
            event("ops-agent", "gateway", head + " is already in the history, skipped",
                repeat ? "DEDUPED" : "LOST (a different step with the same words)");
            return;
        }
        history.add(norm(text));
        applied.add(s.call().effect());
        ran.computeIfAbsent(step, k -> new ArrayList<>()).add(tick);
        missed += repeat ? 1 : 0;
        if (acks) {
            acked.add(step);
        }
        event("gateway", refund ? "meridian" : "mailer", head + (repeat ? ", no match in the history" : ""),
            repeat ? "DUPLICATED (first ran at t=" + ran.get(step).get(0) + ")" : refund ? "EXECUTED" : "SENT");
    }

    public static void main(String[] args) {
        String late = "Refund $20 to order 5101, the parcel arrived late";
        List<Step> quiet = List.of(new Step("s1", new Call("refund", "5101", 20), late),
            new Step("s2", new Call("refund", "5101", 20), late),
            new Step("s3", new Call("email", "5101", 0), "Tell the customer on order 5101 that the refund was issued"));
        List<Step> plan = List.of(
            new Step("s1", new Call("refund", "5102", 65), "Refund $65 to order 5102, item arrived damaged"),
            new Step("s2", new Call("email", "5102", 0), "Tell the customer on order 5102 that the refund was issued"),
            new Step("s3", new Call("refund", "5103", 30), "Refund $30 to order 5103, wrong size shipped"),
            new Step("s4", new Call("email", "5103", 0), "Tell the customer on order 5103 that the refund was issued"));
        // Scripted worst case: after a restart the stub repeats s1 verbatim and rewords s2 and s3.
        Map<String, String> reworded = Map.of("s2", "Let the customer on order 5102 know the refund was issued",
            "s3", "Credit $30 back on order 5103, wrong size shipped");

        section("Setup");
        event("orchestrator", "ops-agent", "prompt rule added: skip any action whose description is in the history", "INFO");
        section("Quiet run run-0416");
        quiet.forEach(s -> work("run-0416", s, s.text(), true));
        boolean legit = exact(intended(quiet));
        int quietRefunds = count(applied, "refund");

        section("Crash mid-run run-0417");
        plan.subList(0, 3).forEach(s -> work("run-0417", s, s.text(), !s.id().equals("s3")));
        event("orchestrator", "ops-agent", "t=" + (++tick) + " worker 1 dies before s3 is acknowledged, "
            + "run-0417 restarts with an empty context", "INFO");
        List<String> completed = acked.stream().filter(k -> k.startsWith("run-0417/")).toList();

        section("Restart and replay");
        plan.forEach(s -> work("run-0417", s, reworded.getOrDefault(s.id(), s.text()), true));

        List<String> want = new ArrayList<>(intended(quiet));
        want.addAll(intended(plan));
        List<String> extra = excess(applied, want), missing = excess(want, applied);
        long again = completed.stream().filter(k -> ran.get(k).size() > 1).count();
        List<String> resent = wordings.keySet().stream().filter(k -> wordings.get(k).size() > 1).toList();
        long changed = resent.stream().filter(k -> new LinkedHashSet<>(wordings.get(k)).size() > 1).count();
        System.out.println("METRIC refunds_applied = " + count(applied, "refund"));
        System.out.println("METRIC dollars_paid = " + dollars(applied));
        System.out.println("METRIC dollars_intended = " + dollars(want));
        System.out.println("METRIC emails_sent = " + count(applied, "email"));
        System.out.println("METRIC dedupes_correct = " + right);
        System.out.println("METRIC dedupes_wrong = " + wrong);
        System.out.println("METRIC repeats_missed = " + missed);
        check("effect-applied-exactly-once", extra.isEmpty() && missing.isEmpty(), "every refund and email applied once",
            count(applied, "refund") + " refunds for " + count(want, "refund") + " intended, yet "
            + count(extra, "refund") + " repeated ($" + dollars(extra) + ") and " + count(missing, "refund")
            + " missing ($" + dollars(missing) + "); emails repeated: " + count(extra, "email"));
        check("resume-skips-completed-steps", again == 0, "no completed step was sent again",
            again + " of " + completed.size() + " completed steps sent again after the restart");
        check("idempotency-key-stable-across-retries", changed == 0, "re-proposed steps kept their wording",
            changed + " of " + resent.size() + " re-proposed steps changed wording, so their identity changed");
        check("legit-uninterrupted-run-works", legit, "run-0416 applied every effect once",
            "only " + quietRefunds + " of " + count(intended(quiet), "refund") + " refunds reached order 5101");
    }
}
