public class Main {
    // A shared logging library is required by every team, and it attaches
    // the SAME field name -- traceId -- propagated from the original
    // request, to every log line either service emits for that request.
    static boolean canCorrelate(LogEntry a, LogEntry b) {
        return a.identifierField.equals(b.identifierField) && a.identifierValue.equals(b.identifierValue);
    }

    public static void main(String[] args) {
        LogEntry fromServiceA = new LogEntry("Service A", "traceId", "req-789", "checkout started");
        LogEntry fromServiceB = new LogEntry("Service B", "traceId", "req-789", "payment processed");

        System.out.println("Service A logged: " + fromServiceA.identifierField + "=" + fromServiceA.identifierValue);
        System.out.println("Service B logged: " + fromServiceB.identifierField + "=" + fromServiceB.identifierValue);

        boolean correlated = canCorrelate(fromServiceA, fromServiceB);
        System.out.println();
        System.out.println("Correlated as the same request: " + correlated);

        System.out.println();
        System.out.println(correlated
            ? "FIXED: both log lines carry the identical traceId, propagated from the same real request, "
              + "because both teams' code runs through the same required logging library. Correlating an "
              + "incident across these two services is now an exact match on one field, not a manual "
              + "reconstruction from timestamps and guesswork."
            : "Unexpected -- investigate.");
    }
}
