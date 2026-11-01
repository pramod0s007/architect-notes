import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Deny everywhere: only a live "yes" runs a call, so a timeout stops every route the same way.
// The repair route sits behind the same engine, and nothing else can bring the engine back.
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

    // The gateway only decides allow or deny. The harness stamps OUTAGE on legitimate reads and
    // repair that the timeout stopped; refunds stay DENIED, because failing closed is right for them.
    static boolean call(String src, String who, String req) {
        String[] r = req.split(" ", 3);
        String route = r[0] + " " + r[1];
        boolean rule = RULES.contains(who + " " + route), live = inTime(), run = live && rule;
        boolean stopped = !live && rule && !route.equals("POST /refunds");
        String verdict = run ? "ALLOWED" : (stopped ? "OUTAGE" : "DENIED");
        String why = live ? (rule ? "policy allows" : "no rule for this caller")
            : "pdp timed out, deny is the default";
        event(src, "gateway", "t=" + now + " " + req + " as " + who, verdict + " (" + why + ")");
        if (run && route.equals("POST /refunds")) ledger.add(new Refund(who, true));
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
            + " tick, default on timeout is deny", null);

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

        section("Locked out");
        at(38);
        event("pdp", "gateway", "t=" + now + (inTime() ? " answers resume" : " still silent, no rollback ran"),
            inTime() ? "OK" : "OUTAGE");
        call("ops-agent", "orbit.ops-agent", "POST /refunds order=7311 amount=25");

        long open = ledger.stream().filter(r -> !r.decided()).count();
        long served = reads.stream().filter(ok -> ok).count();
        System.out.println("METRIC legit_reads_denied = " + (reads.size() - served));
        System.out.println("METRIC repair_calls_denied = " + (fixed ? 0 : 1));
        System.out.println("METRIC engine_down_ticks_so_far = " + (now - deployed));
        check("high-consequence-route-fails-closed", open == 0,
            "every refund on the ledger had a policy answer (" + ledger.size() + ")",
            open + " of " + ledger.size() + " refunds ran with no policy answer");
        String readText = served + " of " + reads.size() + " ops-agent reads served";
        check("legit-low-risk-read-survives-engine-outage", served == reads.size(), readText, readText);
        String fixText = "rollback " + (fixed ? "ran" : "refused") + ", engine "
            + (inTime() ? "answering" : "still down") + " at t=" + now;
        check("legit-repair-survives-engine-outage", fixed && inTime(), fixText, fixText);
    }
}
