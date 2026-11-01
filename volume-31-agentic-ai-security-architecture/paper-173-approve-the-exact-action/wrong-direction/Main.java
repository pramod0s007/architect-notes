import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Wrong direction: ask for approval on every tool call. The screens still show a model-written
 * summary and the executor is still the flag-by-id executor from the pressure program; only the
 * volume changes. The reviewer is a scripted assumption (reads the first READ_BUDGET prompts of a
 * shift, then clicks through), not a measurement of people.
 */
public class Main {
    record Call(String merchant, String order, int amount) {}
    record Prompt(int n, Call shown, Call ran) {}

    static final int SHIFT = 24, READ_BUDGET = 6, LIMIT = 500;
    static int seq = 0;

    static String summary(Call c) {
        return "Refund $" + c.amount() + " for order " + c.order();
    }

    // A reader declines a refund over the limit; a reviewer who only clicks approves everything.
    static boolean approves(Prompt p, int reads) {
        return p.n() > reads || p.shown().amount() <= LIMIT;
    }

    // The executor runs whatever arrives with an approved id, so it runs p.ran(), not p.shown().
    static List<Call> runShift(List<Prompt> shift, int reads) {
        List<Call> ledger = new ArrayList<>();
        for (Prompt p : shift) {
            if (approves(p, reads)) {
                ledger.add(p.ran());
            }
        }
        return ledger;
    }

    static void play(Prompt p, int reads) {
        boolean approve = approves(p, reads);
        event("reviewer", "approvals", "prompt " + p.n() + " \"" + summary(p.shown()) + "\" "
            + (p.n() <= reads ? "read" : "clicked through"), approve ? "APPROVED" : "REFUSED");
        if (approve) {
            String verdict = !p.ran().equals(p.shown()) ? "HIJACKED (the summary said order " + p.shown().order() + ")"
                : p.shown().amount() > LIMIT ? "VIOLATION (over the $" + LIMIT + " limit, never read)" : "EXECUTED";
            event("executor", "ledger", "prompt " + p.n() + " runs refund merchant=" + p.ran().merchant()
                + " order=" + p.ran().order() + " amount=" + p.ran().amount(), verdict);
        }
    }

    static void section(String title) {
        System.out.println("-- " + title + " --");
    }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        Call swap = new Call("m-377", "6188", 6500);
        Call bad = new Call("m-391", "6204", 4800);
        List<Prompt> shift = new ArrayList<>();
        for (int n = 1; n <= SHIFT; n++) {
            Call shown = n == 14 ? bad : new Call("m-204", String.valueOf(6110 + n), 20 + n);
            shift.add(new Prompt(n, shown, n == 3 ? swap : shown));
        }
        Prompt swapPrompt = shift.get(2);
        Prompt badPrompt = shift.get(13);
        List<Call> tired = runShift(shift, READ_BUDGET);
        List<Call> perfect = runShift(shift, SHIFT);

        section("Setup");
        event("ops-team", "approvals", "every tool call now needs a click: " + shift.size() + " prompts this shift",
            "INFO");
        event("approvals", "reviewer", "screens still show the model summary, not the call", "INFO");

        section("A tired reviewer reads " + READ_BUDGET + " prompts, then clicks through");
        play(swapPrompt, READ_BUDGET);
        play(badPrompt, READ_BUDGET);
        long routine = shift.stream().filter(p -> p.shown().equals(p.ran()) && p.shown().amount() <= LIMIT).count();
        long routineRan = tired.stream().filter(c -> c.amount() <= LIMIT).count();
        event("reviewer", "approvals", "the other " + routineRan + " prompts are routine refunds", "APPROVED");

        section("A perfect reviewer reads all " + SHIFT);
        play(swapPrompt, SHIFT);
        play(badPrompt, SHIFT);

        List<Call> bads = List.of(swap, bad);
        long swapRuns = tired.stream().filter(swap::equals).count() + perfect.stream().filter(swap::equals).count();
        System.out.println("METRIC prompts_shown = " + shift.size());
        System.out.println("METRIC clicked_without_reading = " + shift.stream().filter(p -> p.n() > READ_BUDGET).count());
        System.out.println("METRIC bad_refunds_tired_reviewer = " + tired.stream().filter(bads::contains).count());
        System.out.println("METRIC bad_refunds_perfect_reviewer = " + perfect.stream().filter(bads::contains).count());
        check("legit-approved-refund-works", routineRan == routine,
            "all " + routine + " routine refunds ran", routineRan + " of " + routine + " routine refunds ran");
        check("swapped-arguments-rejected", swapRuns == 0,
            "the swapped call never ran", "the swapped call ran in " + swapRuns + " of 2 shifts");
        check("accurate-bad-action-declined", !tired.contains(bad),
            "the tired reviewer declined the accurate $" + bad.amount() + " refund",
            "the tired reviewer approved the accurate $" + bad.amount() + " refund and it ran");
        check("approvals-stay-readable", shift.size() <= READ_BUDGET,
            shift.size() + " prompts, within the " + READ_BUDGET + " a reviewer reads",
            shift.size() + " prompts, over the " + READ_BUDGET + " a reviewer reads");
    }
}
