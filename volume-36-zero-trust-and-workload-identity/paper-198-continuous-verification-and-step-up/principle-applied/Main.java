import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// Meridian asks the pdp on every request: is the token alive, does the proof match the key bound at sign-in (t=01, also the
// first strong authentication), what do the device signals say, how sensitive is the action. A keyed digest stands in for a
// key held by the client. Harness inputs: the posture feed, the tick, which device really sends a request, the token leak
// (EXPOSED only restates it) and the console always answering a step-up. Every verdict comes from this code. No model.
public class Main {
    record Decision(int tick, String action, String device, String verdict, String level, int age, int exp, boolean postureOk) {}
    record Refresh(int tick, String device, boolean postureOk, String outcome) {}
    static final String REFUNDS = "POST /refunds", PAYOUT = "PUT /payout-destination", LAPTOP = "lap-3", BOX = "rt-box";
    static final int TTL = 4, WINDOW = 2;
    static final List<Decision> audit = new ArrayList<>();
    static final List<Refresh> refreshes = new ArrayList<>();
    static final List<String> prompts = new ArrayList<>();
    static String token, level, bound = digest("device-key|" + LAPTOP);
    static boolean feedOk = true;
    static int seq = 0, exp, authTick = 1, issued;

    static String digest(String text) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    // Only the device that owns a key can compute a proof that matches the key the session was bound to.
    static String proof(String device, String secret) { return digest(digest("device-key|" + device) + "|" + secret); }
    static boolean boundTo(String p, String secret) { return p.equals(digest(bound + "|" + secret)); }
    // Harness ground truth for the checks, never read by the enforcement code: lap-3 fails posture from t=05 to t=07.
    static boolean postureOk(int tick) { return tick < 5 || tick > 7; }
    static String t(int tick) { return String.format(Locale.ROOT, "t=%02d", tick); }
    static String signIn(OptionalInt age) { return age.isPresent() ? "sign-in " + age.getAsInt() + " ticks old" : "none"; }

    static void mint(int tick) {
        token = "tok-" + ++issued;
        exp = tick + TTL;
        level = feedOk ? "FULL" : "LIMITED";
    }
    // A refresh needs the bound key and re-reads the latest posture report; it never upgrades on its own say-so.
    static void refresh(String src, int tick, String device) {
        boolean ok = boundTo(proof(device, "rt-1"), "rt-1");
        if (ok) mint(tick);
        refreshes.add(new Refresh(tick, device, postureOk(tick), ok ? level : "REFUSED"));
        event(src, "issuer", t(tick) + " refresh with rt-1 from " + device, ok
            ? "ISSUED (" + token + " valid to " + t(exp) + ", level " + level + " from the latest posture report)"
            : "REFUSED (proof does not match the key bound at sign-in)");
    }

    // A failing report lowers the session at once; only a refresh can raise it again.
    static void report(int tick, boolean ok) {
        feedOk = ok;
        if (!ok) level = "LIMITED";
        event("console", "pdp", t(tick) + " posture report for " + LAPTOP + ": " + (ok ? "compliant" : "noncompliant"),
            ok ? "RECORDED (level " + level + " until the next refresh)" : "CAPPED (level LIMITED at once)");
    }

    // Meridian asks, the pdp answers in this order, and every answer is logged with its reason.
    static String call(String src, int tick, String device, String action, String subject, String extra) {
        int age = tick - authTick;
        String verdict, why;
        if (tick > exp) { verdict = "EXPIRED"; why = token + " valid to " + t(exp); }
        else if (!boundTo(proof(device, token), token)) { verdict = "REJECTED"; why = "proof does not match the key bound at sign-in"; }
        else if (!action.equals(PAYOUT)) { verdict = "ALLOWED"; why = "routine action, no prompt"; }
        else if (level.equals("LIMITED")) { verdict = "DENIED"; why = "session is LIMITED, sensitive actions are off"; }
        else if (age > WINDOW) { verdict = "HELD"; why = "last strong sign-in " + age + " ticks ago, window " + WINDOW; }
        else { verdict = "ALLOWED"; why = "strong sign-in " + age + " ticks ago, inside the window"; }
        audit.add(new Decision(tick, action, device, verdict, level, age, exp, postureOk(tick)));
        event(src, "meridian", t(tick) + " " + action + " " + subject + " " + extra + " from " + device, verdict + " (" + why + ")");
        return verdict;
    }

    // The console reacts to the verdict it gets: refresh when expired, step up when held, then ask again.
    static void operator(int tick, String action, String subject, String extra) {
        String v = call("console", tick, LAPTOP, action, subject, extra);
        if (v.equals("EXPIRED")) { refresh("console", tick, LAPTOP); v = call("console", tick, LAPTOP, action, subject, extra); }
        if (v.equals("HELD")) {
            authTick = tick;
            prompts.add(action);
            event("console", "issuer", t(tick) + " step-up: fresh strong factor for " + action, "VERIFIED (last sign-in is now " + t(tick) + ")");
            call("console", tick, LAPTOP, action, subject, extra);
        }
    }
    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        section("Setup");
        mint(1);
        event("console", "issuer", t(1) + " sign-in on " + LAPTOP + " with a strong factor",
            "ISSUED (" + token + " valid to " + t(exp) + ", refresh token rt-1, both bound to the laptop key)");
        event("pdp", "meridian", "policy: " + PAYOUT + " is sensitive, every other action is routine",
            "OK (step-up window " + WINDOW + " ticks, token life " + TTL + " ticks)");
        section("The operator works and a token copy leaks");
        operator(2, REFUNDS, "order 7801", "amount 40");
        event("report-batch", "report-batch", t(3) + " copies " + token + " and rt-1 from a shared debug log",
            "EXPOSED (premise of the test, stamped by the harness)");
        call("report-batch", 3, BOX, PAYOUT, "m-5120", "to acct-9907");
        call("report-batch", 3, BOX, REFUNDS, "order 7890", "amount 900");
        section("The laptop falls out of compliance");
        report(5, false);
        operator(5, PAYOUT, "m-5188", "to acct-6620");
        operator(5, REFUNDS, "order 7802", "amount 15");
        operator(6, REFUNDS, "order 7803", "amount 25");
        refresh("report-batch", 6, BOX);
        operator(7, PAYOUT, "m-5188", "to acct-6620");
        report(8, true);
        section("The laptop is repaired and the payout change is retried");
        operator(9, PAYOUT, "m-5188", "to acct-6620");
        operator(11, PAYOUT, "m-5188", "to acct-6620");
        operator(13, PAYOUT, "m-5189", "to acct-6621");
        operator(14, PAYOUT, "m-5190", "to acct-6622");
        List<Decision> done = audit.stream().filter(d -> d.verdict().equals("ALLOWED")).toList();
        List<Decision> payouts = done.stream().filter(d -> d.action().equals(PAYOUT)).toList(), failing = audit.stream().filter(d -> !d.postureOk()).toList();
        List<String> outcomes = refreshes.stream().map(Refresh::outcome).toList();
        long tried = audit.stream().filter(d -> d.device().equals(BOX)).count(), fromBox = done.stream().filter(d -> d.device().equals(BOX)).count();
        long full = failing.stream().filter(d -> d.level().equals("FULL")).count(), paid = payouts.stream().filter(d -> !d.postureOk()).count();
        OptionalInt stalest = payouts.stream().mapToInt(Decision::age).max();
        OptionalInt freshest = audit.stream().filter(d -> d.verdict().equals("HELD")).mapToInt(Decision::age).min();
        long wrong = refreshes.stream().filter(r -> !r.outcome().equals(!r.device().equals(LAPTOP) ? "REFUSED" : r.postureOk() ? "FULL" : "LIMITED")).count();
        long routine = done.stream().filter(d -> d.action().equals(REFUNDS) && d.device().equals(LAPTOP)).count();
        long waited = audit.stream().filter(d -> d.verdict().equals("DENIED") && d.postureOk()).count();
        long atEdge = done.stream().filter(d -> d.tick() == d.exp()).count(), after = done.stream().filter(d -> d.tick() > d.exp()).count();
        System.out.println("METRIC requests_evaluated = " + audit.size());
        System.out.println("METRIC payout_changes_executed = " + payouts.size());
        System.out.println("METRIC payouts_from_other_devices = " + payouts.stream().filter(d -> !d.device().equals(LAPTOP)).count());
        System.out.println("METRIC prompts_shown = " + prompts.size());
        System.out.println("METRIC refreshes_granted = " + outcomes.stream().filter(o -> !o.equals("REFUSED")).count());
        check("stolen-token-from-new-device-denied", fromBox == 0 && tried > 0, tried + " requests from " + BOX + " with a copied token, " + fromBox + " executed");
        check("posture-change-downgrades-session", !failing.isEmpty() && full == 0 && paid == 0,
            failing.size() + " decisions while " + LAPTOP + " failed posture, " + full + " at level FULL, " + paid + " payouts executed");
        check("sensitive-action-requires-step-up", payouts.size() == 3 && stalest.orElse(0) <= WINDOW && freshest.orElse(0) > WINDOW,
            payouts.size() + " payout changes executed, stalest executed: " + signIn(stalest) + ", freshest held: " + signIn(freshest) + ", window " + WINDOW);
        check("refresh-reevaluates-signals", wrong == 0 && new TreeSet<>(outcomes).size() == 3 && waited == 1,
            refreshes.size() + " refreshes (" + String.join(", ", outcomes) + "), " + wrong + " differ from the true posture and device, "
            + waited + " payout denied while " + LAPTOP + " was compliant again but not yet refreshed");
        check("legit-routine-action-no-prompt", routine == 3 && !prompts.contains(REFUNDS),
            routine + " routine refunds executed, " + prompts.stream().filter(REFUNDS::equals).count() + " prompts for " + REFUNDS);
        check("expired-token-denied", after == 0 && atEdge > 0, atEdge + " request ran on a token's last valid tick, " + after + " after expiry, "
            + audit.stream().filter(d -> d.verdict().equals("EXPIRED")).count() + " refused as EXPIRED");
    }
}
