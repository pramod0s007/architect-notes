import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Default allow: when the policy engine misses its deadline the gateway lets the call through.
// A bad deploy and a flood of status reads both push the engine past that deadline.
public class Main {
    record Refund(String who, boolean decided) {}

    static final int RATE = 4, DEADLINE = 1, FIX = 4, FLOOD = 40;
    static final List<String> RULES = List.of("orbit.ops-agent POST /refunds",
        "orbit.ops-agent GET /status", "oncall.sre POST /pdp/rollback");
    static final List<Refund> ledger = new ArrayList<>();
    static final List<Boolean> reads = new ArrayList<>();
    static int seq = 0, now = 0, backlog = 0, deployed = 0, downUntil = 0;

    static void at(int t) {
        backlog = Math.max(0, backlog - RATE * (t - now));
        now = t;
    }

    static boolean inTime() { return now >= downUntil && backlog <= RATE * DEADLINE; }

    // The harness replays the rule list to label what a timeout let through; the gateway cannot know.
    static boolean call(String src, String who, String req) {
        String[] r = req.split(" ", 3);
        String route = r[0] + " " + r[1];
        boolean rule = RULES.contains(who + " " + route), live = inTime(), run = !live || rule;
        String verdict = live ? (rule ? "ALLOWED" : "DENIED") : (rule ? "APPROVED" : "VIOLATION");
        String why = live ? (rule ? "policy allows" : "no rule for this caller")
            : "pdp timed out, default is allow";
        event(src, "gateway", "t=" + now + " " + req + " as " + who, verdict + " (" + why + ")");
        if (run && route.equals("POST /refunds")) ledger.add(new Refund(who, live));
        if (run && route.equals("POST /pdp/rollback")) downUntil = now + FIX;
        return run;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        section("Setup");
        event("gateway", "pdp", "every call asks for a decision, deadline " + DEADLINE
            + " tick, default on timeout is allow", null);

        section("Engine up");
        at(18);
        call("ops-agent", "orbit.ops-agent", "POST /refunds order=7310 amount=40");
        call("report-batch", "reports.batch", "POST /refunds order=7388 amount=900");

        section("Engine slow");
        at(20);
        backlog += FLOOD;
        event("report-batch", "gateway", "t=" + now + " " + FLOOD + " x GET /status as reports.batch",
            "QUEUED (backlog " + backlog + ", " + RATE + " finish per tick)");
        at(22);
        call("report-batch", "reports.batch", "POST /refunds order=7388 amount=900");
        at(23);
        reads.add(call("ops-agent", "orbit.ops-agent", "GET /status"));

        section("Engine down");
        at(30);
        deployed = now;
        downUntil = Integer.MAX_VALUE;
        event("pdp", "gateway", "t=" + now + " bad deploy, the new version never answers", "OUTAGE");
        at(31);
        call("ops-agent", "orbit.ops-agent", "POST /refunds order=7311 amount=25");
        at(32);
        reads.add(call("ops-agent", "orbit.ops-agent", "GET /status"));
        at(34);
        boolean fixed = call("console", "oncall.sre", "POST /pdp/rollback");
        at(38);

        long open = ledger.stream().filter(r -> !r.decided()).count();
        long theirs = ledger.stream().filter(r -> !r.decided() && r.who().equals("reports.batch")).count();
        long served = reads.stream().filter(ok -> ok).count();
        System.out.println("METRIC refunds_executed_without_policy = " + open);
        System.out.println("METRIC refunds_for_reports_batch_without_policy = " + theirs);
        System.out.println("METRIC engine_down_ticks = " + (downUntil - deployed));
        check("high-consequence-route-fails-closed", open == 0,
            "every refund on the ledger had a policy answer (" + ledger.size() + ")",
            open + " of " + ledger.size() + " refunds ran with no policy answer, " + theirs
                + " for reports.batch");
        String readText = served + " of " + reads.size() + " ops-agent reads served";
        check("legit-low-risk-read-survives-engine-outage", served == reads.size(), readText, readText);
        String fixText = "rollback " + (fixed ? "ran" : "refused") + ", engine "
            + (inTime() ? "answering" : "still down") + " at t=" + now;
        check("legit-repair-survives-engine-outage", fixed && inTime(), fixText, fixText);
    }
}
