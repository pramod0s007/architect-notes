public class Main {
    // Each team picked its own logging scheme independently. Service A logs
    // a request ID string; Service B logs an unrelated numeric counter.
    // Nothing about the two schemes has any field in common to match on.
    static boolean canCorrelate(LogEntry a, LogEntry b) {
        return a.identifierField.equals(b.identifierField) && a.identifierValue.equals(b.identifierValue);
    }

    public static void main(String[] args) {
        // One real request that touches both services during an incident.
        LogEntry fromServiceA = new LogEntry("Service A", "requestId", "abc-123", "checkout started");
        LogEntry fromServiceB = new LogEntry("Service B", "counterId", "47291", "payment processed");

        System.out.println("Service A logged: " + fromServiceA.identifierField + "=" + fromServiceA.identifierValue);
        System.out.println("Service B logged: " + fromServiceB.identifierField + "=" + fromServiceB.identifierValue);

        boolean correlated = canCorrelate(fromServiceA, fromServiceB);
        System.out.println();
        System.out.println("Correlated as the same request: " + correlated);

        System.out.println();
        System.out.println(!correlated
            ? "CONFIRMED FAILURE: these two log lines are from the exact same real request, investigated "
              + "during the same incident, and there is no way to automatically confirm that -- Service "
              + "A's requestId and Service B's counterId share no common field, no common format, no "
              + "common anything. An engineer has to correlate them by hand, by timestamp and guesswork, "
              + "because the two teams' tooling never agreed on how to identify a request in the first place."
            : "Unexpected -- investigate.");
    }
}
