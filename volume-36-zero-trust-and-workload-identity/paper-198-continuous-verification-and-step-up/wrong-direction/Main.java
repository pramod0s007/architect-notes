import java.util.*;

// Version 1 shortens the token and forces a new sign-in at every expiry; version 2 adds a silent refresh so the
// prompts stop. Expiry is enforced in both. HIJACKED is stamped by the harness from the true device; the CHECK
// lines read the ledger, not the labels.
public class Main {
    record Done(int tick, String action, String subject, String device, int exp) {}
    record Refresh(int tick, String device, boolean postureOk, String outcome) {}

    static final String REFUNDS = "POST /refunds", PAYOUT = "PUT /payout-destination", LAPTOP = "lap-3", BOX = "rt-box";
    static final int TTL = 4;
    static final List<Done> ledger = new ArrayList<>();
    static final List<Refresh> refreshes = new ArrayList<>();
    static final List<String> prompts = new ArrayList<>();
    static String token;
    static boolean silent;
    static int seq = 0, exp, issued, expired;

    // Harness ground truth, never read by Meridian: lap-3 fails its posture check from t=05 and is never repaired.
    static boolean postureOk(int tick) { return tick < 5; }
    static String t(int tick) { return String.format(Locale.ROOT, "t=%02d", tick); }

    static void mint(int tick) {
        token = "tok-" + ++issued;
        exp = tick + TTL;
    }

    // Version 1: a forced sign-in with a strong factor.
    static void signIn(int tick) {
        mint(tick);
        event("console", "issuer", t(tick) + " sign-in with a strong factor", "ISSUED (" + token + " valid to " + t(exp) + ")");
    }

    // Version 2: whoever holds rt-1 gets a new token; the device and the signals are never read.
    static void refresh(int tick, String device) {
        mint(tick);
        refreshes.add(new Refresh(tick, device, postureOk(tick), "FULL"));
        event(device.equals(LAPTOP) ? "console" : "report-batch", "issuer", t(tick) + " refresh with rt-1 from " + device,
            "ISSUED (" + token + " valid to " + t(exp) + ", level FULL, signals not read)");
    }

    // Meridian's rule is the expiry and nothing else; the harness labels what ran on a copied token.
    static boolean call(String src, int tick, String device, String action, String subject, String extra) {
        boolean live = tick <= exp;
        if (live) ledger.add(new Done(tick, action, subject, device, exp));
        else expired++;
        String verdict = !live ? "EXPIRED" : device.equals(LAPTOP) ? "ALLOWED" : "HIJACKED";
        event(src, "meridian", t(tick) + " " + action + " " + subject + " " + extra + " from " + device,
            verdict + " (" + token + " valid to " + t(exp) + ")");
        return live;
    }

    // The operator signs in again in version 1 and refreshes silently in version 2, then retries the same refund.
    static void operator(int tick, String subject, String extra) {
        if (call("console", tick, LAPTOP, REFUNDS, subject, extra)) return;
        if (silent) refresh(tick, LAPTOP);
        else { prompts.add(subject); signIn(tick); }
        call("console", tick, LAPTOP, REFUNDS, subject, extra);
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        section("Version 1: short tokens and a forced sign-in");
        signIn(1);
        event("meridian", "meridian", "rule: tokens live " + TTL + " ticks, an expired token is refused", "OK (nothing else is checked)");
        call("console", 2, LAPTOP, REFUNDS, "order 7801", "amount 40");
        event("report-batch", "report-batch", t(3) + " copies " + token + " from a shared debug log", "EXPOSED (premise of the test, stamped by the harness)");
        call("report-batch", 3, BOX, PAYOUT, "m-5120", "to acct-9907");
        event("console", "pdp", t(5) + " posture report for " + LAPTOP + ": noncompliant", "RECORDED (stored, and no rule reads it)");
        call("report-batch", 6, BOX, REFUNDS, "order 7890", "amount 900");
        operator(6, "order 7803", "amount 25");
        operator(11, "order 7804", "amount 30");
        section("Version 2: a silent refresh so the prompts stop");
        silent = true;
        event("issuer", "console", t(12) + " hands over rt-1: no lifetime, no key, no device or signal check", "ISSUED (" + token + " stays valid to " + t(exp) + ")");
        operator(16, "order 7805", "amount 20");
        event("report-batch", "report-batch", t(17) + " copies rt-1 from the same debug log", "EXPOSED (premise of the test, stamped by the harness)");
        refresh(41, BOX);
        call("report-batch", 41, BOX, PAYOUT, "m-5121", "to acct-9907");
        refresh(95, BOX);
        call("report-batch", 95, BOX, PAYOUT, "m-5122", "to acct-9907");

        List<String> box = ledger.stream().filter(d -> d.device().equals(BOX) && d.action().equals(PAYOUT)).map(Done::subject).toList();
        long routine = ledger.stream().filter(d -> d.action().equals(REFUNDS) && d.device().equals(LAPTOP)).count();
        long wrong = refreshes.stream().filter(r -> !r.outcome().equals(!r.device().equals(LAPTOP) ? "REFUSED" : r.postureOk() ? "FULL" : "LIMITED")).count();
        long after = ledger.stream().filter(d -> d.tick() > d.exp()).count();
        int lastBox = ledger.stream().filter(d -> d.device().equals(BOX)).mapToInt(Done::tick).max().orElse(0);
        int lastLaptop = ledger.stream().filter(d -> d.device().equals(LAPTOP)).mapToInt(Done::tick).max().orElse(0);
        System.out.println("METRIC payout_changes_executed = " + ledger.stream().filter(d -> d.action().equals(PAYOUT)).count());
        System.out.println("METRIC payouts_from_other_devices = " + box.size());
        System.out.println("METRIC prompts_shown = " + prompts.size());
        System.out.println("METRIC refreshes_granted = " + refreshes.size());
        System.out.println("METRIC ticks_session_outlived_operator = " + (lastBox - lastLaptop));
        check("legit-routine-action-no-prompt", prompts.isEmpty(), routine + " routine refunds executed, none interrupted",
            prompts.size() + " of " + routine + " routine refunds stopped at a forced sign-in: " + String.join(", ", prompts));
        check("stolen-token-from-new-device-denied", box.isEmpty(), "no payout change executed from " + BOX,
            box.size() + " payout changes executed from " + BOX + " on a copied token or refresh token: " + String.join(", ", box));
        check("refresh-reevaluates-signals", wrong == 0, "every refresh matched the true device and posture",
            wrong + " of " + refreshes.size() + " refreshes ignored the signals and granted FULL, the thief's included");
        check("expired-token-denied", after == 0, "no request ran after expiry, " + expired + " refused as EXPIRED",
            after + " requests ran after expiry");
    }
}
