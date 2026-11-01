import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Short tokens, one revocation epoch per user, a push feed, and a gateway that fails closed on a stale feed.
public class Main {
    record Token(String id, String sub, String act, int iat, int exp, int epoch) {}
    record Decision(String verdict, String why) { boolean ok() { return verdict.equals("ALLOWED"); } }
    record Write(int tick, String act, String what) {}

    static final String SUB = "admin-3012";
    static final int TTL = 15, LIFETIME_BOUND = 15, STALE_BOUND = 3;
    static final List<Write> LEDGER = new ArrayList<>();
    static int seq = 0;
    static String clock(int tick) { return String.format(Locale.ROOT, "%02d:%02d", tick / 60, tick % 60); }

    static class Authz {
        final Map<String, Integer> epoch = new LinkedHashMap<>();
        final List<Token> issued = new ArrayList<>();

        int current() { return epoch.getOrDefault(SUB, 0); }

        Token mint(Token parent, String act, int now) {
            if (parent != null && parent.epoch() < current()) return null;
            int exp = parent == null ? now + TTL : Math.min(parent.exp(), now + TTL);
            int born = parent == null ? current() : parent.epoch(); // a child keeps its parent's epoch, never a newer one
            Token t = new Token("t-" + (issued.size() + 1), SUB, act, now, exp, born);
            issued.add(t);
            return t;
        }
    }

    static class Gateway {
        final String name;
        final Map<String, Integer> view = new LinkedHashMap<>();
        int cutAt = -1;

        Gateway(String name) { this.name = name; }

        // A live feed delivers a heartbeat every tick; a cut feed ages from the tick it dropped.
        int feedAge(int now) { return cutAt < 0 ? 0 : now - cutAt; }

        Decision decide(Token t, int now) {
            int age = feedAge(now);
            if (now >= t.exp()) return new Decision("EXPIRED", "expired at " + clock(t.exp()));
            if (age > STALE_BOUND) return new Decision("BLOCKED", "feed " + age + " ticks old, bound " + STALE_BOUND);
            int seen = view.getOrDefault(t.sub(), 0);
            if (t.epoch() < seen) return new Decision("REVOKED", "epoch " + t.epoch() + " below " + seen);
            return new Decision("ALLOWED", "epoch " + t.epoch() + ", feed " + age + " ticks old");
        }
    }

    static Token issue(Authz authz, Token parent, String act, int now) {
        Token t = authz.mint(parent, act, now);
        String how = parent == null ? "issue" : "exchange";
        if (t == null) {
            event(parent.act(), "authz", how + " a token for " + act + " with " + parent.id() + " at " + clock(now),
                "REFUSED (parent epoch " + parent.epoch() + " below " + authz.current() + ")");
        } else {
            event("authz", act, how + " " + t.id() + " epoch " + t.epoch() + ", valid " + clock(t.iat()) + "-"
                + clock(t.exp()), "ISSUED");
        }
        return t;
    }

    // The push reaches a gateway only while its feed is up.
    static void push(Authz authz, Gateway g, int now) {
        boolean delivered = g.cutAt < 0;
        if (delivered) g.view.putAll(authz.epoch);
        event("authz", g.name, "disable " + SUB + " at " + clock(now) + ", push epoch " + authz.current(),
            delivered ? "DELIVERED" : "INFO (connection down since " + clock(g.cutAt) + ", push lost)");
    }

    // Every call here is a payout write, so every call is epoch-checked and feed-age-checked.
    static Decision call(Gateway g, Token t, String what, int now) {
        Decision d = g.decide(t, now);
        event(t.act(), g.name, what + " with " + t.id() + " at " + clock(now), d.verdict() + " (" + d.why() + ")");
        if (d.ok()) {
            LEDGER.add(new Write(now, t.act(), what));
            event(g.name, "meridian", what + " for merchant 3012", "EXECUTED");
        }
        return d;
    }

    // Dry run on throwaway state: how many tokens a run of the given length consumes at TTL ticks each.
    static int tokensFor(int span) {
        Authz a = new Authz();
        int n = 1, end = a.mint(null, "agent:ops", 0).exp();
        for (; end < span; n++) end = a.mint(null, "agent:ops", end).exp();
        return n;
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
        Authz authz = new Authz();
        Gateway gw = new Gateway("gateway"), gw2 = new Gateway("gateway-2");
        int legitAt = 835, revokeAt = 841;
        String bad = "set payout destination to acct-9931";

        section("Afternoon at Halcyon: short tokens, an epoch per user, a stale-feed bound");
        Token ops = issue(authz, null, "agent:ops", 830);
        Token recon = issue(authz, ops, "agent:recon", 830);
        Decision legit = call(gw, recon, "set payout schedule to weekly", legitAt);
        gw2.cutAt = 840;
        event("authz", gw2.name, "feed connection drops, last heartbeat at " + clock(gw2.cutAt), "INFO");
        authz.epoch.put(SUB, authz.current() + 1);
        push(authz, gw, revokeAt);
        push(authz, gw2, revokeAt);
        Decision dRecon = call(gw, recon, bad, revokeAt);
        Decision dOps = call(gw, ops, bad, revokeAt);
        Token respawn = issue(authz, ops, "agent:recon", 842);
        Decision dStale = call(gw2, recon, bad, 844);
        call(gw, recon, bad, 860);

        int maxLife = authz.issued.stream().mapToInt(t -> t.exp() - t.iat()).max().orElse(0);
        long late = LEDGER.stream().filter(w -> w.tick() >= revokeAt).count();
        int staleEpoch = gw2.view.getOrDefault(SUB, 0);
        // What the replica with the cut feed would still accept from the revoked child before its bound trips.
        int open = 0;
        for (int now = revokeAt; now < recon.exp(); now++) open += gw2.decide(recon, now).ok() ? 1 : 0;
        System.out.println("METRIC writes_after_revoke = " + late);
        System.out.println("METRIC max_token_lifetime_ticks = " + maxLife);
        System.out.println("METRIC outage_exposure_ticks = " + open);
        System.out.println("METRIC tokens_for_four_hour_run = " + tokensFor(240));
        check("revoked-chain-denied-immediately", !dRecon.ok() && late == 0,
            "child call denied at " + clock(revokeAt) + ", the tick of the revoke, and no write followed",
            late + " writes landed after the revoke");
        check("revocation-cascades-to-sub-agents", !dRecon.ok() && !dOps.ok() && respawn == null,
            "child and parent tokens denied, new spawn refused", "a member of the chain still worked");
        check("stale-revocation-feed-fails-closed", dStale.verdict().equals("BLOCKED") && staleEpoch < authz.current(),
            "replica still at epoch " + staleEpoch + ", feed " + gw2.feedAge(844) + " ticks old, call blocked",
            "replica acted on a view older than the bound");
        check("legit-active-chain-works", legit.ok() && LEDGER.stream().anyMatch(w -> w.tick() == legitAt),
            "schedule changed at " + clock(legitAt) + " under the live chain", "legit write refused");
        check("token-lifetime-bounded", maxLife <= LIFETIME_BOUND, "longest token " + maxLife + " ticks, bound "
            + LIFETIME_BOUND, "longest token " + maxLife + " ticks, bound " + LIFETIME_BOUND);
    }
}
