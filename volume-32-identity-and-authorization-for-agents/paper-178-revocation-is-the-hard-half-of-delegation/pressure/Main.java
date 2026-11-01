import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// The gateway verifies signature and expiry and nothing else. No message ever tells it that the
// account behind a chain was disabled, so a four-hour chain keeps acting until its tokens expire.
public class Main {
    record Token(String id, String act, int iat, int exp) {}
    record Write(int tick, String act, String destination) {}

    static final int LIFETIME = 240;
    static final int LIFETIME_BOUND = 15;
    static final List<Token> ISSUED = new ArrayList<>();
    static final List<Write> LEDGER = new ArrayList<>();
    static int seq = 0;

    static String clock(int tick) { return String.format(Locale.ROOT, "%02d:%02d", tick / 60, tick % 60); }

    static Token issue(String how, String act, int now) {
        Token t = new Token("t-" + (ISSUED.size() + 1), act, now, now + LIFETIME);
        ISSUED.add(t);
        event("authz", act, how + " " + t.id() + " for admin-3012, valid " + clock(t.iat()) + "-" + clock(t.exp()),
            "ISSUED");
        return t;
    }

    // Stateless verification: the account status is never consulted.
    static boolean call(Token t, String what, int now) {
        boolean ok = now < t.exp();
        event(t.act(), "gateway", what + " with " + t.id() + " at " + clock(now),
            ok ? "ALLOWED (signature and expiry valid)" : "EXPIRED");
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
        int disabledAt = 841;
        int attackAt = 860;
        section("Afternoon at Halcyon: a four-hour chain outlives the person behind it");
        Token ops = issue("issue", "agent:ops", 830);
        Token recon = issue("exchange", "agent:recon", 830);
        boolean read = call(ops, "read payout report for merchant 3012", 835);
        event("admin-3012", "authz", "offboarding disables the account at " + clock(disabledAt), "DONE");
        if (call(recon, "set payout destination to acct-9931", attackAt)) {
            LEDGER.add(new Write(attackAt, recon.act(), "acct-9931"));
            // Harness label: the gateway cannot know this. The harness stamps ABUSED because authz had
            // already disabled the account when the write executed.
            event("gateway", "meridian", "payout destination for merchant 3012 changed to acct-9931",
                attackAt >= disabledAt ? "ABUSED" : "EXECUTED");
        }

        List<Write> late = LEDGER.stream().filter(w -> w.tick() >= disabledAt).toList();
        int lastExp = ISSUED.stream().mapToInt(Token::exp).max().orElse(0);
        int maxLife = ISSUED.stream().mapToInt(t -> t.exp() - t.iat()).max().orElse(0);
        int gap = late.isEmpty() ? 0 : late.get(0).tick() - disabledAt;
        System.out.println("METRIC exposure_after_disable_ticks = " + (lastExp - disabledAt));
        System.out.println("METRIC writes_after_revoke = " + late.size());
        System.out.println("METRIC max_token_lifetime_ticks = " + maxLife);
        check("revoked-chain-denied-immediately", late.isEmpty(), "no write after the account was disabled",
            "a write landed " + gap + " ticks after the account was disabled");
        check("token-lifetime-bounded", maxLife <= LIFETIME_BOUND, "longest token " + maxLife + " ticks",
            "longest token " + maxLife + " ticks, bound " + LIFETIME_BOUND);
        check("legit-active-chain-works", read, "report read at " + clock(835) + " while the account was active",
            "legit read refused");
    }
}
