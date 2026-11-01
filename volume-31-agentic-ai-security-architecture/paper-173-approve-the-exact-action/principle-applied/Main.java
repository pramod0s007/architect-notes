import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// The approval is bound to the exact call. The screen is rendered from the canonical call, the digest
// covers every field the executor relies on (expiry and nonce included), and the executor re-derives
// the digest from the object it is about to run. The agent is the same scripted worst case.
public class Main {
    record ToolCall(String tool, String merchant, String order, int amount, int expiresAt, String nonce) {}
    record Entry(ToolCall call, int tick) {}
    record Result(boolean ok, String verdict, String reason) {}

    static final int TTL = 30, LOW_LIMIT = 50, READ_BUDGET = 6, REVIEW_LIMIT = 500;
    static final Map<String, String> approved = new LinkedHashMap<>(); // nonce -> digest the reviewer saw
    static final Map<String, String> shown = new LinkedHashMap<>(); // nonce -> screen text the reviewer read
    static final Set<String> used = new LinkedHashSet<>();
    static final List<Entry> ledger = new ArrayList<>();
    static int seq = 0, nonces = 0, prompts = 0, rejections = 0;

    // Length-prefixed fields: a crafted merchant value cannot smuggle in a field boundary.
    static String canonical(ToolCall c) {
        StringBuilder sb = new StringBuilder();
        for (String f : List.of(c.tool(), c.merchant(), c.order(), "" + c.amount(), "" + c.expiresAt(), c.nonce())) {
            sb.append(f.length()).append(':').append(f).append(';');
        }
        return sb.toString();
    }

    static String digest(ToolCall c) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(canonical(c).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    static String id(String d) { return d.substring(0, 8); }

    // The screen is rendered from the same object that is hashed: no model-written text on it.
    static String render(ToolCall c) {
        return "Refund $" + c.amount() + " for order " + c.order() + " to merchant " + c.merchant()
            + ", valid to tick " + c.expiresAt() + ", id " + id(digest(c));
    }

    static String desc(ToolCall c) {
        return "refund merchant=" + c.merchant() + " order=" + c.order() + " amount=" + c.amount();
    }

    static ToolCall ask(String merchant, String order, int amount, int now) {
        String nonce = String.format(Locale.ROOT, "n-%04d", ++nonces);
        ToolCall c = new ToolCall("refund", merchant, order, amount, now + TTL, nonce);
        shown.put(nonce, render(c));
        event("approvals", "reviewer", "screen \"" + shown.get(nonce) + "\"", "PENDING");
        // Scripted reviewer: reads while the queue is short and declines big refunds; later it only clicks.
        boolean yes = ++prompts > READ_BUDGET || amount <= REVIEW_LIMIT;
        if (yes) approved.put(nonce, digest(c));
        event("reviewer", "approvals", (yes ? "approves " : "declines ") + nonce,
            yes ? "APPROVED" : "REFUSED (accurate screen, over the $" + REVIEW_LIMIT + " review limit)");
        return c;
    }

    static Result no(String verdict, String reason) { return new Result(false, verdict, reason); }

    static Result execute(ToolCall c, int now) {
        String d = digest(c); // from the object about to run, never copied from the approval
        boolean high = c.amount() > LOW_LIMIT;
        if (high) {
            String seen = approved.get(c.nonce());
            if (seen == null) return no("REJECTED", "no approval for " + c.nonce());
            if (!seen.equals(d)) return no("REJECTED", "digest " + id(d) + " differs from approved " + id(seen));
            // Expiry is inside the digest, so once the digest matches it is the one the approver minted.
            if (now > c.expiresAt()) return no("EXPIRED", "approval ended at tick " + c.expiresAt() + ", now " + now);
            if (!used.add(c.nonce())) return no("REJECTED", "nonce already used");
        }
        ledger.add(new Entry(c, now));
        return new Result(true, high ? "VERIFIED" : "ALLOWED", high ? "digest matches, unexpired, unused" : "tier LOW");
    }

    static void attempt(ToolCall c, int now) {
        Result r = execute(c, now);
        event("ops-agent", "executor", desc(c) + " with " + c.nonce() + " at tick " + now,
            r.verdict() + " (" + r.reason() + ")");
        if (r.ok()) event("executor", "ledger", desc(c), "EXECUTED");
        else rejections++;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        section("Routine refunds stay out of the queue");
        for (int i = 1; i <= 21; i++) execute(new ToolCall("refund", "m-204", "r-" + i, 20 + i, 100 + TTL, "low-" + i), 100);
        event("ops-agent", "executor", ledger.size() + " routine refunds of $" + LOW_LIMIT + " or less, no prompt", "ALLOWED");
        section("A high-risk refund is approved");
        ToolCall a = ask("m-204", "6120", 400, 100);
        section("The call that runs is not the call that was shown");
        ToolCall swapped = new ToolCall("refund", "m-377", "6188", 6500, a.expiresAt(), a.nonce());
        attempt(swapped, 101);
        attempt(a, 102);
        section("The approval is presented again");
        attempt(a, 103);
        section("An approval used too late");
        attempt(ask("m-204", "6140", 400, 104), 900);
        section("A reviewer with time declines an accurate screen");
        ToolCall bad = ask("m-391", "6204", 4800, 905);
        if (approved.containsKey(bad.nonce())) attempt(bad, 906);

        // The checks re-derive each digest from what actually ran, independent of the executor's own bookkeeping.
        List<Entry> high = ledger.stream().filter(e -> e.call().amount() > LOW_LIMIT).toList();
        List<Entry> unapproved = high.stream().filter(e -> !digest(e.call()).equals(approved.get(e.call().nonce()))).toList();
        long pastExpiry = high.stream().filter(e -> e.tick() > e.call().expiresAt()).count();
        long uses = ledger.stream().filter(e -> e.call().nonce().equals(a.nonce())).count();
        String matched = (high.size() - unapproved.size()) + " of " + high.size() + " high-tier refunds match an approved digest";
        String golden = "Refund $400 for order 6120 to merchant m-204, valid to tick 130, id " + id(digest(a));
        System.out.println("METRIC refunds_executed = " + ledger.size());
        System.out.println("METRIC human_prompts = " + prompts);
        System.out.println("METRIC executions_rejected = " + rejections);
        System.out.println("METRIC dollars_not_as_approved = " + unapproved.stream().mapToInt(e -> e.call().amount()).sum());
        check("legit-approved-refund-works", ledger.stream().anyMatch(e -> e.call().equals(a)),
            "order 6120, $400, ran as approved", "order 6120 was approved but did not run");
        check("approved-digest-matches-executed", !high.isEmpty() && unapproved.isEmpty(), matched, matched);
        check("swapped-arguments-rejected", ledger.stream().noneMatch(e -> e.call().equals(swapped)),
            "order 6188 never reached the ledger", "order 6188 refunded $" + swapped.amount());
        check("approval-single-use", uses <= 1, "nonce " + a.nonce() + " ran " + uses + " of 1 allowed",
            "nonce " + a.nonce() + " ran " + uses + " of 1 allowed");
        check("approval-expires", pastExpiry == 0,
            "no refund ran after its approval ended", pastExpiry + " refund ran after its approval ended");
        check("screen-shows-executed-action", golden.equals(shown.get(a.nonce())),
            "the screen for " + a.nonce() + " lists the amount, order, merchant and expiry that ran",
            "the screen for " + a.nonce() + " does not match the call that ran");
        check("approvals-stay-readable", prompts <= READ_BUDGET,
            prompts + " prompts, within the " + READ_BUDGET + " a reviewer reads",
            prompts + " prompts, over the " + READ_BUDGET + " a reviewer reads");
        check("accurate-bad-action-declined", ledger.stream().noneMatch(e -> e.call().equals(bad)),
            "the accurate $" + bad.amount() + " refund never ran", "the accurate $" + bad.amount() + " refund ran");
    }
}
