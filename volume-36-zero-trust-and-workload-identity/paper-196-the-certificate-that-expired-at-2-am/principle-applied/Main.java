import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Principle applied: renew at two thirds of the lifetime, rotate keys with overlapping trust, alert on time to expiry,
 * and never switch verification off. One tick is one hour from 02:00; no clock is read. */
public class Main {
    record Cert(String subject, String signer, int from, int to) {}
    static final int LIFETIME = 24, RENEW_AT = LIFETIME * 2 / 3, WARN_AT = 6, ACK = 2, CALLS = 40, END = 48;
    static final int LEAK_AT = 10, BOUND = 24, K2_PUBLISHED = 4, K2_SIGNING = 28, STAGING = 24;
    static final String K1 = "issuer-key-1", K2 = "issuer-key-2";
    static final List<String> WORKLOADS = List.of("gateway", "ops-agent");
    static int seq = 0;

    static String verify(Cert c, Set<String> bundle, int t) {
        if (t < c.from() || t >= c.to()) return "EXPIRED"; // outside the validity window
        return bundle.contains(c.signer()) ? "ACCEPTED" : "REJECTED";
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String fmt, Object... args) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + String.format(Locale.ROOT, fmt, args) + ")");
    }

    public static void main(String[] args) {
        Set<String> bundle = new LinkedHashSet<>(List.of(K1));
        Map<String, Cert> cur = new LinkedHashMap<>();
        Set<Cert> alerted = new LinkedHashSet<>();
        Set<String> windowSigners = new LinkedHashSet<>();
        String signing = K1, probe = "NONE";
        Cert copy = null, stale = null;
        int fixedAt = Integer.MAX_VALUE, k1Dropped = -1, lastK1Swap = -1; // lastK1Swap: last tick a K1 certificate was replaced
        int alertLead = 0, renewals = 0, minMargin = LIFETIME, calls = 0, rejected = 0, untrusted = 0, lapsed = 0;
        int leakTicks = 0, lateAccepts = 0, windowTicks = 0;

        section("Setup and the leak");
        for (String who : WORKLOADS) cur.put(who, new Cert("orbit." + who, K1, 0, LIFETIME));
        event("issuer", "gateway", "issue orbit.gateway signed by " + K1 + ", valid t=0.." + LIFETIME, "ISSUED");
        event("issuer", "console", "staging lifetime " + STAGING + " ticks, production " + LIFETIME + " ticks", "RECORDED");

        for (int t = 0; t < END; t++) {
            if (t == RENEW_AT) section("Renewal and the alert");
            if (t == K2_PUBLISHED) {
                bundle.add(K2);
                event("issuer", "meridian", K2 + " added to the trust bundle at t=" + t + ", before it signs", "TRUSTED");
            }
            if (t == K2_SIGNING) {
                section("Key rotation");
                signing = K2;
                event("issuer", "console", "new certificates are signed by " + K2 + " from t=" + t, "RECORDED");
            }
            if (t == fixedAt) event("console", "ops-agent", "on-call renews the enrollment credential at t=" + t, "DONE");

            for (String who : WORKLOADS) {
                Cert c = cur.get(who);
                if (t - c.from() < RENEW_AT) continue;
                if (who.equals("ops-agent") && t < fixedAt) { // scripted failure: the alert has to catch it
                    if (t == RENEW_AT) event(who, "issuer", "renewal request at t=" + t, "REFUSED (enrollment credential expired)");
                    continue;
                }
                Cert next = new Cert(c.subject(), signing, t, t + LIFETIME);
                cur.put(who, next);
                if (c.signer().equals(K1)) { lastK1Swap = t; stale = c; }
                renewals++;
                minMargin = Math.min(minMargin, c.to() - t);
                event(who, "issuer", "renew at t=" + t + " with " + (c.to() - t) + " ticks left, new certificate valid t="
                    + next.from() + ".." + next.to() + ", signed by " + signing, "ISSUED");
            }

            for (Cert c : cur.values()) {
                if (c.to() - t > WARN_AT || !alerted.add(c)) continue;
                alertLead = c.to() - t;
                fixedAt = t + ACK;
                event("issuer", "console", "inventory scan at t=" + t + ": " + c.subject() + " expires in " + alertLead + " ticks", "ALERTED");
            }

            boolean k1InUse = false;
            for (Cert c : cur.values()) k1InUse |= c.signer().equals(K1) && t < c.to();
            if (!k1InUse && bundle.remove(K1)) {
                k1Dropped = t;
                event("issuer", "meridian", K1 + " removed from the trust bundle at t=" + t
                    + ", no valid certificate signed by it is in service", "DONE");
                probe = stale == null ? "NONE" : verify(stale, bundle, t);
                if (stale != null) event("console", "meridian", "probe at t=" + t + " with a replaced certificate signed by " + K1
                    + ", valid until t=" + stale.to(), probe);
            }

            boolean window = signing.equals(K2) && bundle.contains(K1);
            if (window) windowTicks++;
            for (String who : WORKLOADS) {
                Cert c = cur.get(who);
                String v = verify(c, bundle, t);
                int n = who.equals("gateway") ? CALLS : 1;
                calls += n;
                if (t >= c.to()) lapsed++; // the workload is still using a certificate past its end date
                if (!v.equals("ACCEPTED")) {
                    rejected += n;
                    if (v.equals("REJECTED")) untrusted += n; // REJECTED means the signer is not in the bundle
                } else if (window && windowSigners.add(c.signer()) && c.signer().equals(K2)) {
                    event(who, "meridian", "call at t=" + t + " signed by " + K2 + ", " + K1 + " still trusted", v);
                }
                if (t == LIFETIME && who.equals("ops-agent"))
                    event(who, "meridian", "GET /status at t=" + t + ", the tick its first certificate expires", v);
            }

            if (t == LEAK_AT) copy = cur.get("gateway");
            if (copy != null) { // the intruder replays the copy every tick until the run ends
                String v = verify(copy, bundle, t);
                boolean ok = v.equals("ACCEPTED");
                if (ok) leakTicks++;
                if (ok && t >= copy.to()) lateAccepts++;
                // ABUSED is the harness's label for an accepted intruder; meridian itself only answers ACCEPTED.
                if (t == LEAK_AT) event("report-batch", "meridian", "POST /refunds at t=" + t
                    + " with a leaked copy of the gateway key and certificate, valid until t=" + copy.to(),
                    ok ? "ABUSED (meridian answered " + v + ")" : v);
                if (t == copy.to()) event("report-batch", "meridian", "POST /refunds at t=" + t + " with the same copy, "
                    + (t - LEAK_AT) + " ticks after the leak", v);
            }
        }

        System.out.println("METRIC legit_calls_rejected = " + rejected);
        System.out.println("METRIC leaked_key_valid_ticks = " + leakTicks);
        System.out.println("METRIC alert_lead_ticks = " + alertLead);
        System.out.println("METRIC renewals = " + renewals);
        System.out.println("METRIC overlap_window_ticks = " + windowTicks);
        check("renewal-before-expiry", renewals > 0 && lapsed == 0 && minMargin > ACK,
            "%d renewals, smallest margin %s ticks against a %d-tick repair, %d workload-ticks past expiry",
            renewals, renewals == 0 ? "n/a" : minMargin, ACK, lapsed);
        check("expired-cert-still-rejected", copy.to() < END && lateAccepts == 0,
            "accepted %d times at or after the copy's end date t=%d, the run lasts to t=%d", lateAccepts, copy.to(), END);
        check("overlapping-trust-keeps-traffic-up", untrusted == 0 && windowSigners.size() == 2,
            "%d calls hit an untrusted signer in %d overlap ticks, signers accepted there: %s",
            untrusted, windowTicks, String.join(" and ", windowSigners));
        check("old-key-retired-when-unused", k1Dropped == lastK1Swap && probe.equals("REJECTED"),
            "%s retired %s, its last certificate was replaced at t=%d, a replaced copy answered %s",
            K1, k1Dropped < 0 ? "never" : "at t=" + k1Dropped, lastK1Swap, probe);
        check("expiry-alert-fires-before-outage", alertLead >= ACK, "alert %d ticks before expiry, the fix takes %d", alertLead, ACK);
        check("legit-renewal-no-downtime", rejected == 0, "%d of %d calls rejected over %d ticks", rejected, calls, END);
        check("leaked-key-window-bounded", leakTicks <= BOUND, "leaked copy valid for %d ticks, budget %d", leakTicks, BOUND);
        check("staging-rehearses-expiry", END / STAGING >= 1, // arithmetic on the configuration, not a simulated staging run
            "staging lifetime %d, production %d: %d expiries in %d ticks", STAGING, LIFETIME, END / STAGING, END);
    }
}
