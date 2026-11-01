import java.util.Locale;
import java.util.Set;

/**
 * Pressure: the gateway certificate is renewed by hand. The renewal lives in a calendar reminder owned by
 * someone who left the rota, and staging runs a lifetime four times longer, so nothing ever failed early.
 * One tick is one hour of story time and tick 0 is 02:00 on the day of issue; the program reads no clock.
 */
public class Main {
    record Cert(String subject, String signer, int from, int to) {}

    static final int LIFETIME = 2160, STAGING = 8640, CALLS = 40, REMINDER_AT = 2100, END = 2170;
    static final int ACK = 1, APPROVAL = 2, INSTALL = 1, RESTART = 1; // manual recovery steps, in ticks
    static int seq = 0;

    static String verify(Cert c, Set<String> bundle, int t) {
        if (!bundle.contains(c.signer())) return "REJECTED";
        return t >= c.from() && t < c.to() ? "ACCEPTED" : "EXPIRED";
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
        boolean reminderSeen = false; // the owner changed teams: the entry fires in a calendar nobody on the rota reads
        int failAt = 0;
        while (verify(gw, bundle, failAt).equals("ACCEPTED")) failAt++;
        String atExpiry = verify(gw, bundle, failAt);
        int issuedAt = failAt + ACK + APPROVAL;
        int backAt = issuedAt + INSTALL + RESTART;
        Cert next = new Cert(gw.subject(), gw.signer(), issuedAt, issuedAt + LIFETIME);
        int firstWarning = reminderSeen ? REMINDER_AT : failAt; // otherwise the first symptom page is the warning

        int servedBefore = 0, rejected = 0;
        for (int t = 0; t < END; t++) {
            if (verify(t >= backAt ? next : gw, bundle, t).equals("ACCEPTED")) {
                if (t < gw.to()) servedBefore += CALLS;
            } else {
                rejected += CALLS;
            }
        }

        section("Setup");
        event("issuer", "gateway", "issue " + gw.subject() + " signed by " + gw.signer() + ", valid t=" + gw.from()
            + ".." + gw.to() + " (" + LIFETIME / 24 + " days at 1 tick per hour)", "ISSUED");

        section("The reminder");
        // MISSED is the harness's label: the reminder fired, but nobody who could act saw it.
        event("console", "console", "calendar reminder at t=" + REMINDER_AT + " fires for an owner who changed teams",
            reminderSeen ? "ALERTED" : "MISSED");

        section("Expiry");
        event("gateway", "meridian", "POST /refunds at t=" + (gw.to() - 1) + ", the last tick of the certificate",
            verify(gw, bundle, gw.to() - 1));
        // OUTAGE is the harness's label for rejecting a legitimate caller; Meridian itself only answers EXPIRED.
        event("gateway", "meridian", "POST /refunds at t=" + failAt + ", " + CALLS + " calls per tick, certificate expired",
            "OUTAGE (meridian answered " + atExpiry + ")");
        event("meridian", "console", "refund success rate fell to 0 at t=" + failAt + ", first page of the incident", "ALERTED");

        section("Manual recovery");
        event("console", "issuer", "on-call acknowledges at t=" + (failAt + ACK)
            + ", finds the owner and an approver, requests a new certificate", "PENDING");
        event("issuer", "console", "new certificate issued at t=" + issuedAt + ", valid t=" + next.from() + ".." + next.to(),
            "ISSUED");
        event("console", "gateway", "install the certificate and restart, done at t=" + backAt, "DONE");
        event("gateway", "meridian", "POST /refunds at t=" + backAt + " with the new certificate", verify(next, bundle, backAt));

        int lead = gw.to() - firstWarning;
        int stagingExpiries = LIFETIME / STAGING;
        System.out.println("METRIC refund_calls_rejected = " + rejected);
        System.out.println("METRIC outage_ticks = " + rejected / CALLS);
        System.out.println("METRIC alert_lead_ticks = " + lead);
        check("legit-call-while-valid", servedBefore == CALLS * gw.to(),
            "%d of %d calls accepted before expiry", servedBefore, CALLS * gw.to());
        check("expired-cert-still-rejected", atExpiry.equals("EXPIRED"), "meridian answered %s at t=%d", atExpiry, failAt);
        check("renewal-before-expiry", backAt <= gw.to(), "certificate expired at t=%d, replacement installed at t=%d", gw.to(), backAt);
        check("expiry-alert-fires-before-outage", lead > 0, "first warning at t=%d, %d ticks before expiry", firstWarning, lead);
        check("legit-renewal-no-downtime", rejected == 0, "%d refund calls rejected over %d ticks", rejected, rejected / CALLS);
        check("staging-rehearses-expiry", stagingExpiries > 0,
            "staging lifetime %d vs production %d: %d expiries in %d ticks", STAGING, LIFETIME, stagingExpiries, LIFETIME);
    }
}
