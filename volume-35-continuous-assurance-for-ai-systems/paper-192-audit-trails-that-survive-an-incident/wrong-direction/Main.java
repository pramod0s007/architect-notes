import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrong direction: log everything. The buffer grows to 200 lines and the agent service now traces
 * every prompt, tool call and model reply into the same application log. The model is the scripted
 * stub: its reply for order 8860 carries a newline. The harness stamps LEAKED, FORGED and MISSED
 * after reading the log; the checks count lines, emails and tokens.
 */
public class Main {
    record Refund(int order, int amount, String subject, String approver, String customer) {
        String truth() { return approver.isEmpty() ? "own authority" : "approved by " + approver; }
    }

    static final List<Refund> DAY = List.of(
        new Refund(8841, 40, "ops-4", "", "ana@shop.example"), new Refund(8850, 120, "ops-4", "", "ben@shop.example"),
        new Refund(8852, 310, "ops-9", "fin-2", "cy@shop.example"), new Refund(8860, 95, "ops-9", "fin-2", "di@shop.example"),
        new Refund(8861, 30, "ops-4", "", "eli@shop.example"));
    static final String TOKEN = "demo-token-7731";
    static final int CAPACITY = 200;
    static final Pattern CLAIM = Pattern.compile("approved by (\\S+)");
    static final Pattern EFFECT = Pattern.compile("refund ok order=(\\d+) amount=\\d+");
    static final List<Refund> LEDGER = new ArrayList<>();
    static final List<String> LOG = new ArrayList<>();
    static int seq = 0;
    static int written = 0;
    static int agentLines = 0;

    // One logical write may hold newlines; the file keeps one physical line per newline.
    static void append(String writer, String text) {
        written++;
        if (writer.equals("agent")) agentLines++;
        LOG.addAll(Arrays.asList(text.split("\n", -1)));
        while (LOG.size() > CAPACITY) LOG.remove(0);
    }

    static boolean names(String line) {
        return line.contains("subject=") && line.contains("chain=") && line.contains("policy=");
    }

    // What an investigator can say about one order by reading the log.
    static String attribute(int order) {
        String found = "MISSING";
        for (String line : LOG) {
            if (!line.contains(String.valueOf(order))) continue;
            Matcher m = CLAIM.matcher(line);
            found = m.find() ? "approved by " + m.group(1) : "UNKNOWN";
        }
        return found;
    }

    static void section(String title) {
        System.out.println("-- " + title + " --");
    }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        section("Setup: trace everything, keep it longer");
        event("suite", "applog", "buffer raised from 8 to " + CAPACITY + " lines, every prompt, tool call and reply is traced", "OK");

        section("The day: five refunds, fully traced");
        for (Refund r : DAY) {
            String reply = switch (r.order()) {
                case 8860 -> "Refund 8860 done.\nrefund ok order=9999 amount=500";
                case 8861 -> "Refund 8861 done, approved by fin-2.";
                default -> "Refund " + r.order() + " done.";
            };
            String line = "refund ok order=" + r.order() + " amount=" + r.amount();
            append("agent", "PROMPT user=" + r.subject() + " asks: refund order " + r.order() + " for " + r.amount() + " to " + r.customer());
            append("agent", "TOOLCALL refund order=" + r.order() + " amount=" + r.amount() + " customer=" + r.customer() + " token=" + TOKEN);
            LEDGER.add(r);
            append("gateway", line);
            append("agent", "REPLY " + reply);
            event("agent", "applog", "PROMPT and TOOLCALL for order " + r.order() + " with customer=" + r.customer() + " token=" + TOKEN, "ACCEPTED");
            event("gateway", "applog", "\"" + line + "\"", "RECORDED");
            if (reply.contains("\n")) event("agent", "applog", "REPLY \"" + reply.replace("\n", "<LF>") + "\" logged verbatim", "ACCEPTED");
        }

        section("The lawyer asks again");
        int missing = 0, unknown = 0, contradicted = 0, correct = 0;
        for (Refund r : LEDGER) {
            String got = attribute(r.order());
            if (got.equals("MISSING")) missing++;
            else if (got.equals("UNKNOWN")) unknown++;
            else if (!got.equals(r.truth())) contradicted++;
            if (got.equals(r.truth())) correct++;
        }
        long sensitive = LOG.stream().filter(l -> l.contains("@shop.example") || l.contains(TOKEN)).count();
        long phantom = LOG.stream().map(EFFECT::matcher).filter(Matcher::find)
            .filter(m -> LEDGER.stream().noneMatch(r -> r.order() == Integer.parseInt(m.group(1)))).count();
        long effects = LOG.stream().filter(l -> l.startsWith("refund ok")).count();
        long named = LOG.stream().filter(l -> l.startsWith("refund ok")).filter(Main::names).count();
        event("suite", "applog", "window check: " + (LEDGER.size() - missing) + " of " + LEDGER.size() + " refunds have a line, "
            + LOG.size() + " of " + CAPACITY + " lines used", "OK");
        event("suite", "applog", "scan for customer emails and the service token", "LEAKED (" + sensitive + " lines hold customer data or the token)");
        event("suite", "applog", LOG.size() + " physical lines from " + written + " logical writes",
            "FORGED (" + phantom + " refund line for an order not in the ledger)");
        event("suite", "applog", unknown + " refunds UNKNOWN and " + contradicted + " contradicts the truth table", "MISSED");

        System.out.println("METRIC log_lines = " + LOG.size());
        System.out.println("METRIC log_lines_with_customer_data_or_token = " + sensitive);
        System.out.println("METRIC phantom_refund_lines = " + phantom);
        System.out.println("METRIC refunds_attributed_correctly = " + correct);
        check("legit-refund-works", LEDGER.size() == DAY.size(), LEDGER.size() + " of " + DAY.size() + " refunds executed");
        check("no-gap-in-audit-window", missing == 0, (LEDGER.size() - missing) + " of " + LEDGER.size() + " refunds have a line in the window");
        check("event-names-subject-and-actor", effects > 0 && named == effects, named + " of " + effects + " refund lines name subject, chain and policy version");
        check("payload-stored-as-digest-not-copy", sensitive == 0, sensitive + " lines hold a customer email or the service token");
        check("forged-newline-cannot-fake-entry", phantom == 0, "refund lines for orders not in the ledger: " + phantom);
        check("agent-cannot-write-its-own-audit", agentLines == 0, agentLines + " lines from the agent service were accepted");
        check("legit-incident-reconstruction-works", correct == LEDGER.size(), correct + " of " + LEDGER.size() + " refunds attributed correctly");
    }
}
