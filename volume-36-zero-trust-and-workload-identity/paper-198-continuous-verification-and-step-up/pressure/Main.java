import java.util.*;

// Meridian's only rule: a token that has not expired is a valid session. HIJACKED and VIOLATION are stamped by the
// harness from facts only it knows (the true device, the true posture); the CHECK lines read the ledger, not the labels.
public class Main {
    record Done(int tick, String action, String device, int age, boolean postureOk) {}

    static final String REFUNDS = "POST /refunds", PAYOUT = "PUT /payout-destination", LAPTOP = "lap-3", BOX = "rt-box";
    static final int SIGN_IN = 1, TTL = 12, WINDOW = 2;
    static final List<Done> ledger = new ArrayList<>();
    static int seq = 0, prompts = 0;

    // Harness ground truth, never read by Meridian: lap-3 fails its posture check from t=05 to t=07.
    static boolean postureOk(int tick) { return tick < 5 || tick > 7; }
    static String t(int tick) { return String.format(Locale.ROOT, "t=%02d", tick); }

    // The whole decision is "has the token expired?". The device, the signals and the action are never consulted,
    // and prompts has no writer because this design has no step-up path.
    static void call(String src, int tick, String device, String action, String subject, String extra, String stamp) {
        int age = tick - SIGN_IN;
        ledger.add(new Done(tick, action, device, age, postureOk(tick)));
        event(src, "meridian", t(tick) + " " + action + " " + subject + " " + extra + " from " + device,
            stamp + " (tok-1 valid to " + t(SIGN_IN + TTL) + ", sign-in age " + age + ", nothing else asked)");
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        section("Setup");
        event("console", "issuer", t(SIGN_IN) + " sign-in on " + LAPTOP + " with a strong factor", "ISSUED (tok-1 valid to " + t(SIGN_IN + TTL) + ")");
        event("meridian", "meridian", "rule: a token that has not expired is a valid session", "OK (one check at sign-in, none after)");
        section("A routine day and a token copy");
        call("console", 2, LAPTOP, REFUNDS, "order 7801", "amount 40", "ALLOWED");
        event("report-batch", "report-batch", t(3) + " copies tok-1 from a shared debug log", "EXPOSED (premise of the test, stamped by the harness)");
        call("report-batch", 3, BOX, PAYOUT, "m-5120", "to acct-9907", "HIJACKED");
        section("The laptop falls out of compliance");
        event("console", "pdp", t(5) + " posture report for " + LAPTOP + ": noncompliant", "RECORDED (stored, and no rule reads it)");
        call("console", 7, LAPTOP, PAYOUT, "m-5188", "to acct-6620", "VIOLATION");
        call("console", 8, LAPTOP, REFUNDS, "order 7802", "amount 25", "ALLOWED");

        List<Done> payouts = ledger.stream().filter(d -> d.action().equals(PAYOUT)).toList();
        long other = payouts.stream().filter(d -> !d.device().equals(LAPTOP)).count();
        long bad = payouts.stream().filter(d -> !d.postureOk()).count();
        long stale = payouts.stream().filter(d -> d.age() > WINDOW).count();
        long routine = ledger.stream().filter(d -> d.action().equals(REFUNDS)).count();
        System.out.println("METRIC payout_changes_executed = " + payouts.size());
        System.out.println("METRIC payouts_from_other_devices = " + other);
        System.out.println("METRIC payouts_while_noncompliant = " + bad);
        System.out.println("METRIC payouts_past_window = " + stale);
        System.out.println("METRIC prompts_shown = " + prompts);
        check("stolen-token-from-new-device-denied", other == 0, "no payout change executed from " + BOX,
            "payout changes executed from " + BOX + " on a token copied from a log: " + other);
        check("posture-change-downgrades-session", bad == 0, "no payout change ran while " + LAPTOP + " failed posture",
            "payout changes executed while " + LAPTOP + " was noncompliant: " + bad);
        check("sensitive-action-requires-step-up", stale == 0, "every payout change ran on a recent sign-in",
            "payout changes executed on a sign-in older than the " + WINDOW + "-tick window: " + stale + ", prompts shown: " + prompts);
        check("legit-routine-action-no-prompt", routine == 2 && prompts == 0, routine + " routine refunds executed with " + prompts + " prompts",
            routine + " of 2 routine refunds executed, prompts shown: " + prompts);
    }
}
