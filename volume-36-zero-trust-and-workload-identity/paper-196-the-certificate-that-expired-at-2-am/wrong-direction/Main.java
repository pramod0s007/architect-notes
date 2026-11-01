import java.util.Locale;
import java.util.Set;

/**
 * Wrong direction: stretch the lifetime to a year, keep the calendar reminder, and when the reminder is missed
 * switch off the expiry check late at night. One tick is one hour of story time and tick 0 is 02:00 on the day
 * of issue; the program reads no clock.
 */
public class Main {
    record Cert(String subject, String signer, int from, int to) {}

    static final int LIFETIME = 8760, REMINDER_AT = 8700, LEAK_AT = 3000, CALLS = 40, BOUND = 24;
    static final int BYPASS_DELAY = 2, REPLAY_AT = 8770, END = 8771;
    static int seq = 0;

    // With expiryOn false the time window is not checked at all, which is what the 2 a.m. switch does.
    static String verify(Cert c, Set<String> bundle, int t, boolean expiryOn) {
        if (!bundle.contains(c.signer())) return "REJECTED";
        return !expiryOn || (t >= c.from() && t < c.to()) ? "ACCEPTED" : "EXPIRED";
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String fmt, Object... args) {
        String detail = String.format(Locale.ROOT, fmt, args);
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        Set<String> bundle = Set.of("issuer-key-1");
        Cert gw = new Cert("orbit.gateway", "issuer-key-1", 0, LIFETIME);
        Cert copy = gw; // the intruder's copy, taken at LEAK_AT
        boolean reminderSeen = false; // the owner changed teams: the entry fires in a calendar nobody on the rota reads
        int renewedAt = reminderSeen ? REMINDER_AT + 1 : -1;
        int failAt = 0;
        while (verify(gw, bundle, failAt, true).equals("ACCEPTED")) failAt++;
        int bypassAt = failAt + BYPASS_DELAY;
        int firstWarning = reminderSeen ? REMINDER_AT : failAt; // otherwise the first symptom page is the warning

        int servedBefore = 0, rejected = 0, leakTicks = 0;
        for (int t = 0; t < END; t++) {
            String v = verify(gw, bundle, t, t < bypassAt);
            if (v.equals("ACCEPTED") && t < gw.to()) servedBefore += CALLS;
            if (!v.equals("ACCEPTED")) rejected += CALLS;
            if (t >= LEAK_AT && verify(copy, bundle, t, true).equals("ACCEPTED")) leakTicks++; // valid with the check on
        }
        String replay = verify(copy, bundle, REPLAY_AT, REPLAY_AT < bypassAt);

        section("Setup");
        event("issuer", "gateway", "issue " + gw.subject() + " signed by " + gw.signer() + ", valid t=" + gw.from()
            + ".." + gw.to() + " (" + LIFETIME / 24 + " days at 1 tick per hour)", "ISSUED");

        section("The leak");
        // ABUSED is the harness's label for an accepted intruder; meridian itself only answers ACCEPTED.
        event("report-batch", "meridian", "POST /refunds at t=" + LEAK_AT + " with a leaked copy of the gateway key and certificate,"
            + " valid for " + leakTicks + " more ticks (" + leakTicks / 24 + " days)",
            "ABUSED (meridian answered " + verify(copy, bundle, LEAK_AT, true) + ")");

        section("The reminder");
        // MISSED is the harness's label: the reminder fired, but nobody who could act saw it.
        event("console", "console", "calendar reminder at t=" + REMINDER_AT + " fires for an owner who changed teams",
            reminderSeen ? "ALERTED" : "MISSED");

        section("Expiry");
        // OUTAGE is the harness's label for rejecting a legitimate caller; meridian itself only answers EXPIRED.
        event("gateway", "meridian", "POST /refunds at t=" + failAt + ", " + CALLS + " calls per tick, certificate expired",
            "OUTAGE (meridian answered " + verify(gw, bundle, failAt, true) + ")");
        event("meridian", "console", "refund success rate fell to 0 at t=" + failAt + ", first page of the incident", "ALERTED");

        section("The late-night fix");
        event("console", "meridian", "on-call sets verify_expiry=false at t=" + bypassAt + " to stop the page", "DONE");
        event("gateway", "meridian", "POST /refunds at t=" + bypassAt + " with the expired certificate, expiry check off",
            verify(gw, bundle, bypassAt, false));

        section("The replay");
        event("report-batch", "meridian", "POST /refunds at t=" + REPLAY_AT + " with the copy, " + (REPLAY_AT - copy.to())
            + " ticks after it expired", "ABUSED (meridian answered " + replay + ")");

        int lead = gw.to() - firstWarning;
        int expiredAccepted = REPLAY_AT >= copy.to() && replay.equals("ACCEPTED") ? 1 : 0;
        System.out.println("METRIC refund_calls_rejected = " + rejected);
        System.out.println("METRIC leaked_key_valid_ticks = " + leakTicks);
        System.out.println("METRIC expired_intruder_calls_accepted = " + expiredAccepted);
        check("legit-call-while-valid", servedBefore == CALLS * gw.to(),
            "%d of %d calls accepted before expiry", servedBefore, CALLS * gw.to());
        check("renewal-before-expiry", renewedAt >= 0 && renewedAt <= gw.to(),
            "certificate expired at t=%d, %s", gw.to(), renewedAt < 0 ? "never replaced" : "replaced at t=" + renewedAt);
        check("expired-cert-still-rejected", replay.equals("EXPIRED"),
            "meridian answered %s for the copy at t=%d, %d ticks after expiry", replay, REPLAY_AT, REPLAY_AT - copy.to());
        check("leaked-key-window-bounded", leakTicks <= BOUND, "leaked copy valid for %d ticks, budget %d", leakTicks, BOUND);
        check("expiry-alert-fires-before-outage", lead > 0, "first warning at t=%d, %d ticks before expiry", firstWarning, lead);
        check("legit-renewal-no-downtime", rejected == 0, "%d refund calls rejected over %d ticks", rejected, rejected / CALLS);
    }
}
