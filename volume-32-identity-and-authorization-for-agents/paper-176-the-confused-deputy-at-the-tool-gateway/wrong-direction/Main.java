import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Wrong direction: the same gateway and the same broad credential, plus two additions. The merchant id
 * must be well formed and known, and the agent's prompt says to ask only for its own merchant.
 * The scripted worst-case stub ignores the prompt (a real model may follow it most of the time, which is
 * a probability, not access control) and asks for a real merchant that belongs to someone else.
 */
public class Main {
    record Token(String sub, String act, String aud, Set<String> scopes, Set<Integer> merchants, int exp) {}
    record Call(String backend, Token token, int merchant) {}

    static final int NOW = 10;
    static final Set<Integer> DIRECTORY = Set.of(3012, 4417);
    static final Map<Integer, Integer> ORDERS = Map.of(3012, 212, 4417, 1380);
    static final Map<Integer, int[]> PAYOUTS = Map.of(
        3012, new int[] {6200, 5840, 6200}, 4417, new int[] {18500, 21000, 17400, 19700, 16000});
    static final Map<String, String> NEEDS = Map.of("atlas", "orders:read", "meridian", "payouts:read");
    static final Token CALLER = new Token("admin-3012", "agent:ops", "gateway",
        Set.of("orders:read", "payouts:read"), Set.of(3012), 60);
    // One service credential, accepted by every backend (aud=*) and covering every merchant.
    static final Token GATEWAY_OWN = new Token("svc:gateway", "svc:gateway", "*",
        Set.of("orders:read", "payouts:read", "payouts:write"), Set.of(3012, 4417), 999);
    static final List<Token> ISSUED = List.of(CALLER, GATEWAY_OWN);
    static final List<Call> SERVED = new ArrayList<>();
    static int seq = 0;
    static int rejected = 0;

    static int total(int merchant) { return Arrays.stream(PAYOUTS.get(merchant)).sum(); }

    static String rows(String backend, int merchant) {
        return backend.equals("atlas") ? ORDERS.get(merchant) + " orders"
            : PAYOUTS.get(merchant).length + " payouts, $" + total(merchant);
    }

    static String show(Token t) {
        return "act=" + t.act() + ", aud=" + t.aud() + ", merchants=" + new TreeSet<>(t.merchants())
            + ", scopes=" + new TreeSet<>(t.scopes());
    }

    // True when a token carries the caller's user and agent and nothing wider.
    static boolean within(Token t) {
        return t.sub().equals(CALLER.sub()) && t.act().equals(CALLER.act())
            && CALLER.merchants().containsAll(t.merchants()) && CALLER.scopes().containsAll(t.scopes());
    }

    // Shape and existence are the only questions this validation can answer.
    static String validate(String id) {
        if (!id.matches("[0-9]{4}")) {
            return "malformed id";
        }
        return DIRECTORY.contains(Integer.parseInt(id)) ? null : "unknown merchant";
    }

    // A backend authorizes the credential it is handed; it cannot see whom the gateway is serving.
    static boolean serve(String backend, Token t, int merchant) {
        String need = NEEDS.get(backend);
        boolean ok = t.scopes().contains(need) && t.merchants().contains(merchant);
        if (ok) {
            SERVED.add(new Call(backend, t, merchant));
        }
        event("gateway", backend, "read " + need.substring(0, need.indexOf(':')) + ", merchant " + merchant
            + ", as " + t.sub(), ok ? "ALLOWED (" + rows(backend, merchant) + ")"
            : "DENIED (credential does not cover it)");
        return ok;
    }

    // The gateway checks who is asking and whether the id is real, never whether they may ask for it.
    static void gateway(Token t, String id) {
        String ask = "payout summary, merchant " + id;
        if (!ISSUED.contains(t) || !t.aud().equals("gateway") || t.exp() <= NOW) {
            event("agent:ops", "gateway", ask, "REJECTED (token not valid for the gateway)");
            return;
        }
        String problem = validate(id);
        if (problem != null) {
            rejected++;
            event("agent:ops", "gateway", ask, "REJECTED (" + problem + ")");
            return;
        }
        int merchant = Integer.parseInt(id);
        event("agent:ops", "gateway", ask, "ACCEPTED (id is well formed and known)");
        boolean atlas = serve("atlas", GATEWAY_OWN, merchant);
        boolean meridian = serve("meridian", GATEWAY_OWN, merchant);
        if (atlas && meridian) {
            deliver(t, merchant);
        }
    }

    // Harness labelling only: the gateway above never compares the merchant with the caller's token.
    static void deliver(Token caller, int merchant) {
        String verdict = caller.merchants().contains(merchant) ? "DELIVERED"
            : "LEAKED (caller holds merchants " + new TreeSet<>(caller.merchants()) + ")";
        event("gateway", "agent:ops", "report for merchant " + merchant, verdict);
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
        section("Setup");
        event("authz", "agent:ops", "token sub=" + CALLER.sub() + " (" + show(CALLER) + ")", "ISSUED");
        event("authz", "gateway", "credential sub=" + GATEWAY_OWN.sub() + " (" + show(GATEWAY_OWN) + ")", "ISSUED");
        event("orbit", "agent:ops", "system prompt says to ask only for your own merchant", "INFO");

        section("A legitimate report");
        gateway(CALLER, "3012");
        section("The model ignores the prompt");
        String[] invalid = {"44170", "9001"};
        for (String id : invalid) {
            gateway(CALLER, id);
        }
        gateway(CALLER, "4417");

        List<Call> foreign = SERVED.stream().filter(c -> !CALLER.merchants().contains(c.merchant())).toList();
        int exposed = foreign.stream().filter(c -> c.backend().equals("meridian")).mapToInt(c -> total(c.merchant())).sum();
        long wider = SERVED.stream().filter(c -> !within(c.token())).count();
        List<String> own = SERVED.stream().filter(c -> c.merchant() == 3012).map(Call::backend).toList();

        System.out.println("METRIC invalid_ids_rejected = " + rejected);
        System.out.println("METRIC foreign_reads_served = " + foreign.size());
        System.out.println("METRIC foreign_payout_dollars_exposed = " + exposed);
        check("invalid-merchant-id-rejected",
            rejected == invalid.length && SERVED.stream().allMatch(c -> DIRECTORY.contains(c.merchant())),
            rejected + " malformed or unknown ids stopped at the gateway", "an invalid id got past the gateway");
        check("legit-own-merchant-report-works", own.size() == 2,
            "merchant 3012 report served by atlas and meridian",
            "merchant 3012 report was not served by both backends");
        check("cross-merchant-read-denied", foreign.isEmpty(),
            "no read for another merchant was served",
            foreign.size() + " backend reads served for a merchant the caller does not hold");
        check("downstream-call-uses-caller-authority", wider == 0,
            "every backend call ran with the caller's authority",
            wider + " of " + SERVED.size() + " backend calls ran as svc:gateway, wider than the caller");
    }
}
