public class Main {
    static boolean canCorrelate(LogEntry a, LogEntry b) {
        return a.identifierField.equals(b.identifierField) && a.identifierValue.equals(b.identifierValue);
    }

    // The CI gate requires a non-empty traceId field on every deploy, with
    // no exception process at all. It cannot tell a real traceId apart from
    // one that only exists to satisfy the check.
    static boolean passesGate(LogEntry entry) {
        return entry.identifierField.equals("traceId") && !entry.identifierValue.isEmpty();
    }

    public static void main(String[] args) {
        // Team C is mid-migration to the shared library and hasn't wired up
        // real traceId propagation yet. They have an urgent security patch
        // that must ship today.
        LogEntry realAttemptWithoutTraceId = new LogEntry("Service C", "internalId", "req-555", "patch deployed");
        System.out.println("Team C's real deploy, before the required field exists: gate passes = "
            + passesGate(realAttemptWithoutTraceId));

        System.out.println();
        System.out.println("Blocked, with no exception process and an urgent security patch to ship,");
        System.out.println("Team C hardcodes a placeholder traceId just to satisfy the gate:");

        // Two DIFFERENT, unrelated real requests, both logged with the same
        // meaningless placeholder value, because that's what the gate check
        // actually required -- a non-empty field, not a real one.
        LogEntry firstRealRequest = new LogEntry("Service C", "traceId", "unknown", "patch deployed for request 1");
        LogEntry secondRealRequest = new LogEntry("Service C", "traceId", "unknown", "patch deployed for request 2");

        System.out.println("Gate passes now: " + passesGate(firstRealRequest));

        boolean falselyCorrelated = canCorrelate(firstRealRequest, secondRealRequest);
        System.out.println();
        System.out.println("Two DIFFERENT, unrelated real requests -- correlated as the same request: " + falselyCorrelated);

        System.out.println();
        System.out.println(falselyCorrelated
            ? "CONFIRMED FAILURE: the gate passes, and every log line technically has a traceId -- but "
              + "it's the same meaningless placeholder on every single request Team C's service handles, "
              + "because the gate only checked that the field was non-empty, not that it was real. Two "
              + "completely unrelated requests now look, to any automated correlation, like the same "
              + "request. This is worse than having no traceId at all: absence is an obvious gap an "
              + "engineer knows to work around by hand, and a shared fake value is silent, confident, "
              + "and wrong."
            : "Unexpected -- investigate.");
    }
}
