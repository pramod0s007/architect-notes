import static java.nio.charset.StandardCharsets.UTF_8;
import java.security.*;
import java.util.*;

// The outage answer is decided per route: refunds fail closed, low-risk reads fall back to a signed bundle that
// expires, and repair uses a separate audited, alerted door that never asks the engine. Callers, ticks and the
// engine's state come from the harness; OUTAGE and QUEUED label the harness's own scripted engine faults.
public class Main {
    record Bundle(int issuedAt, List<String> grants, String sig) {}
    record Refund(String who, boolean decided) {}
    record Hit(String grant, int age) {}
    static final int RATE = 4, DEADLINE = 1, FIX = 4, FLOOD = 40, MAX_AGE = 14;
    static final List<String> LOW_RISK = List.of("GET /status");
    static final List<String> RULES = List.of("orbit.ops-agent POST /refunds", "orbit.ops-agent GET /status");
    static final String KEY = digest("halcyon-bundle-demo"), SEAL = digest("halcyon-break-glass-demo");
    static final List<Refund> ledger = new ArrayList<>();
    static final List<Boolean> reads = new ArrayList<>();
    static final List<Hit> served = new ArrayList<>(), expired = new ArrayList<>();
    static final List<String> audit = new ArrayList<>(), alerts = new ArrayList<>();
    static Bundle active = new Bundle(0, List.of(), "");
    static int seq = 0, now = 0, backlog = 0, downUntil = 0, breakGlass = 0;

    static String digest(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(UTF_8))); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }

    // Toy signature: a keyed digest over the issue tick and the grants. A real engine signs with a private key.
    static String sign(int issuedAt, List<String> grants) { return digest(KEY + "|" + issuedAt + "|" + grants); }
    static boolean same(String a, String b) { return MessageDigest.isEqual(a.getBytes(UTF_8), b.getBytes(UTF_8)); }
    static void at(int t) { backlog = Math.max(0, backlog - RATE * (t - now)); now = t; }
    static boolean inTime() { return now >= downUntil && backlog <= RATE * DEADLINE; }
    static boolean offer(int t, String src, String what, Bundle b) {
        at(t);
        boolean ok = same(b.sig(), sign(b.issuedAt(), b.grants()));
        if (ok) active = b;
        event(src, "gateway", "t=" + now + " bundle " + what,
            ok ? "ACCEPTED (signature ok)" : "REJECTED (bad signature)");
        return ok;
    }

    static String fallback(String grant) {
        int age = now - active.issuedAt();
        if (!active.grants().contains(grant)) return "DENIED (no grant in the signed bundle)";
        if (age > MAX_AGE) {
            expired.add(new Hit(grant, age));
            return "EXPIRED (bundle age " + age + " is over " + MAX_AGE + ")";
        }
        served.add(new Hit(grant, age));
        return "ALLOWED (last-known-good bundle, age " + age + " of " + MAX_AGE + ")";
    }

    static boolean glass(int t, String src, String who, String secret) {
        at(t);
        boolean ok = same(secret, SEAL);
        breakGlass++;
        audit.add(who + (ok ? " rolled the engine back" : " tried a rollback"));
        alerts.add("break-glass by " + who);
        String trail = "audit " + audit.size() + ", alert " + alerts.size();
        event(src, "gateway", "t=" + now + " POST /pdp/rollback as " + who, ok
            ? "EXECUTED (break-glass, " + trail + ", engine back at t=" + (now + FIX) + ")"
            : "DENIED (break-glass credential wrong, " + trail + ")");
        if (ok) downUntil = now + FIX;
        return ok;
    }

    static boolean call(int t, String src, String who, String req) {
        at(t);
        String[] r = req.split(" ", 3);
        String route = r[0] + " " + r[1], grant = who + " " + route;
        boolean live = inTime();
        String d = live ? (RULES.contains(grant) ? "ALLOWED (policy allows)" : "DENIED (no rule for this caller)")
            : LOW_RISK.contains(route) ? fallback(grant) : "DENIED (pdp timed out, route fails closed)";
        event(src, "gateway", "t=" + now + " " + req + " as " + who, d);
        if (d.startsWith("ALLOWED") && route.equals("POST /refunds")) ledger.add(new Refund(who, live));
        return d.startsWith("ALLOWED");
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }
    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        section("Setup");
        event("gateway", "pdp", "every call asks for a decision, deadline " + DEADLINE
            + " tick, only " + LOW_RISK + " may use a stale bundle", null);
        Bundle first = new Bundle(18, RULES, sign(18, RULES));
        section("Engine up");
        offer(18, "pdp", "pulled, " + RULES.size() + " grants, good for " + MAX_AGE + " ticks", first);
        call(18, "ops-agent", "orbit.ops-agent", "POST /refunds order=7310 amount=40");
        call(18, "report-batch", "reports.batch", "POST /refunds order=7388 amount=900");
        section("Engine slow");
        at(20); backlog += FLOOD;
        event("report-batch", "gateway", "t=" + now + " " + FLOOD + " x GET /status as reports.batch",
            "QUEUED (backlog " + backlog + ", " + RATE + " finish per tick)");
        call(22, "report-batch", "reports.batch", "POST /refunds order=7388 amount=900");
        reads.add(call(23, "ops-agent", "orbit.ops-agent", "GET /status"));
        section("Engine down");
        at(30); downUntil = Integer.MAX_VALUE;
        event("pdp", "gateway", "t=" + now + " bad deploy, the new version never answers", "OUTAGE");
        call(31, "ops-agent", "orbit.ops-agent", "POST /refunds order=7311 amount=25");
        List<Boolean> forged = new ArrayList<>();
        forged.add(offer(31, "report-batch", "with an extra grant, old tick 18, old signature",
            new Bundle(18, List.of(RULES.get(0), RULES.get(1), "reports.batch GET /status"), first.sig())));
        call(31, "report-batch", "reports.batch", "GET /status");
        forged.add(offer(31, "report-batch", "with a fresh tick 31, old signature",
            new Bundle(31, RULES, first.sig())));
        forged.add(offer(31, "report-batch", "with no signature", new Bundle(31, RULES, "")));
        reads.add(call(32, "ops-agent", "orbit.ops-agent", "GET /status"));
        call(33, "ops-agent", "orbit.ops-agent", "GET /status");
        section("Break-glass repair");
        boolean guess = glass(34, "report-batch", "reports.batch", digest("guess"));
        boolean fixed = glass(34, "console", "oncall.sre", SEAL);
        call(38, "ops-agent", "orbit.ops-agent", "POST /refunds order=7311 amount=25");
        long blind = ledger.stream().filter(r -> !r.decided()).count();
        long readsOk = reads.stream().filter(x -> x).count(), bad = forged.stream().filter(x -> x).count();
        List<String> payers = ledger.stream().map(Refund::who).toList();
        System.out.println("METRIC refunds_executed_without_policy = " + blind);
        System.out.println("METRIC forged_bundles_accepted = " + bad);
        System.out.println("METRIC break_glass_calls = " + breakGlass);
        check("high-consequence-route-fails-closed", blind == 0,
            (ledger.size() - blind) + " of " + ledger.size() + " refunds had a policy answer");
        check("legit-refunds-only-for-listed-caller", payers.equals(List.of("orbit.ops-agent", "orbit.ops-agent")),
            "refunds ran for " + payers);
        check("last-known-good-policy-expires",
            served.stream().allMatch(h -> h.age() <= MAX_AGE) && !expired.isEmpty(),
            "served at ages " + served.stream().map(Hit::age).toList() + ", refused at ages "
            + expired.stream().map(Hit::age).toList() + ", bound " + MAX_AGE);
        check("bundle-serves-only-granted-callers",
            !served.isEmpty() && served.stream().allMatch(h -> RULES.contains(h.grant())),
            "bundle served " + served.stream().map(Hit::grant).distinct().toList());
        check("break-glass-path-audited-and-alerted",
            breakGlass > 0 && audit.size() == breakGlass && alerts.size() == breakGlass,
            breakGlass + " calls, " + audit.size() + " audit records, " + alerts.size() + " alerts");
        check("tampered-bundle-rejected", bad == 0 && active.equals(first),
            "forged offers accepted " + bad + " of " + forged.size() + ", bundle in force issued at t="
            + active.issuedAt() + " with " + active.grants().size() + " grants");
        check("legit-low-risk-read-survives-engine-outage", readsOk == reads.size(),
            readsOk + " of " + reads.size() + " ops-agent reads inside the bound served");
        check("legit-repair-survives-engine-outage", fixed && inTime(),
            "rollback ran=" + fixed + ", engine answering=" + inTime() + " at t=" + now);
        check("break-glass-needs-sealed-credential", !guess && fixed,
            "wrong credential ran=" + guess + ", sealed credential ran=" + fixed);
    }
}
