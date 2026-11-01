import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// The tempting fix: revoke the token the user's session holds and let gateways pull the revocation list
// every 15 ticks. The list names token ids, so a token exchanged at runtime is never on it, and the
// pull interval is the attacker's window.
public class Main {
    record Token(String id, String act, int iat, int exp) {}
    record Write(int tick, String act, String destination) {}

    static final int LIFETIME = 240;
    static final int LIFETIME_BOUND = 15;
    static final int REFRESH = 15;
    static final List<Token> ISSUED = new ArrayList<>();
    static final List<Write> LEDGER = new ArrayList<>();
    static final Set<String> REVOKED = new LinkedHashSet<>();
    static Set<String> gatewayCopy = new LinkedHashSet<>();
    static int clockNow = 830;
    static int listedAt = -1;
    static int seq = 0;

    static String clock(int tick) { return String.format(Locale.ROOT, "%02d:%02d", tick / 60, tick % 60); }

    static Token issue(String how, String act, int now) {
        Token t = new Token("t-" + (ISSUED.size() + 1), act, now, now + LIFETIME);
        ISSUED.add(t);
        event("authz", act, how + " " + t.id() + " for admin-3012, valid " + clock(t.iat()) + "-" + clock(t.exp()),
            "ISSUED");
        return t;
    }

    // The gateway only ever sees its last copy of the list, refreshed on every REFRESH-th tick.
    static void advance(int to) {
        for (int t = clockNow + 1; t <= to; t++) {
            if (t % REFRESH != 0) continue;
            gatewayCopy = new LinkedHashSet<>(REVOKED);
            if (listedAt < 0 && !REVOKED.isEmpty()) listedAt = t;
            event("gateway", "authz", "pull revocation list at " + clock(t) + ", entries: " + gatewayCopy.size(),
                "DONE");
        }
        clockNow = to;
    }

    static boolean call(Token t, String what, int now) {
        advance(now);
        boolean listed = gatewayCopy.contains(t.id());
        boolean ok = now < t.exp() && !listed;
        String verdict = now >= t.exp() ? "EXPIRED" : listed ? "REVOKED (" + t.id() + " is on the gateway list)"
            : "ALLOWED (" + t.id() + " is not on the gateway list)";
        event(t.act(), "gateway", what + " with " + t.id() + " at " + clock(now), verdict);
        return ok;
    }

    static boolean change(Token t, String dest, int now, int revokedAt) {
        boolean ok = call(t, "set payout destination to " + dest, now);
        if (ok) {
            LEDGER.add(new Write(now, t.act(), dest));
            // Harness label: the gateway cannot know this. The harness stamps ABUSED because authz had
            // already revoked the chain's authority when the write executed.
            event("gateway", "meridian", "payout destination for merchant 3012 changed to " + dest,
                now >= revokedAt ? "ABUSED" : "EXECUTED");
        }
        return ok;
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
        int revokedAt = 841;
        section("Afternoon at Halcyon: revoke the token, pull the list every " + REFRESH + " ticks");
        Token ops = issue("issue", "agent:ops", 830);
        Token recon = issue("exchange", "agent:recon", 830);
        boolean read = call(ops, "read payout report for merchant 3012", 835);
        advance(revokedAt);
        event("admin-3012", "authz", "offboarding disables the account and revokes " + ops.id() + " at "
            + clock(revokedAt), "REVOKED");
        REVOKED.add(ops.id());
        change(ops, "acct-9931", 845, revokedAt);
        change(ops, "acct-9931", 860, revokedAt);
        boolean childWorked = change(recon, "acct-9931", 860, revokedAt);

        List<Write> late = LEDGER.stream().filter(w -> w.tick() >= revokedAt).toList();
        int maxLife = ISSUED.stream().mapToInt(t -> t.exp() - t.iat()).max().orElse(0);
        int gap = late.isEmpty() ? 0 : late.get(0).tick() - revokedAt;
        System.out.println("METRIC window_ticks = " + (listedAt - revokedAt));
        System.out.println("METRIC writes_after_revoke = " + late.size());
        System.out.println("METRIC max_token_lifetime_ticks = " + maxLife);
        check("revoked-chain-denied-immediately", late.isEmpty(), "no write after the revoke",
            "first write landed " + gap + " ticks after the revoke");
        check("revocation-cascades-to-sub-agents", !childWorked, "child token denied after the revoke",
            recon.id() + " was never listed and still worked at " + clock(860));
        check("token-lifetime-bounded", maxLife <= LIFETIME_BOUND, "longest token " + maxLife + " ticks",
            "longest token " + maxLife + " ticks, bound " + LIFETIME_BOUND);
        check("legit-active-chain-works", read, "report read at " + clock(835) + " while the account was active",
            "legit read refused");
    }
}
