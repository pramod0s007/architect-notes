public class Main {
    static final int WASTED_INVESTIGATION_MINUTES = 15; // time Service A's team spends before ruling themselves out

    public static void main(String[] args) {
        // The alert is routed to whichever service directly shows the
        // symptom -- Service A, where elevated checkout latency is
        // observed. The actual root cause is a slow downstream dependency
        // inside Service B, which nothing about the alert's routing knew.
        System.out.println("Alert fires: checkout latency high. Routed to: Service A (symptom is visible here).");
        System.out.println("t=0: Service A on-call begins investigating.");

        int timeServiceAStarts = 0;
        int timeServiceBStarts = timeServiceAStarts + WASTED_INVESTIGATION_MINUTES;

        System.out.println("t=" + WASTED_INVESTIGATION_MINUTES + ": Service A rules out their own service, escalates to Service B.");
        System.out.println("t=" + timeServiceBStarts + ": Service B on-call -- the team that can actually fix this -- begins investigating.");

        System.out.println();
        System.out.println("Time until the team that can actually fix the root cause starts working on it: " + timeServiceBStarts + " minutes");

        System.out.println();
        System.out.println(timeServiceBStarts == WASTED_INVESTIGATION_MINUTES
            ? "CONFIRMED FAILURE: 15 real minutes were spent before the team that could actually address "
              + "the root cause even started looking, because the alert was routed to whichever service "
              + "happened to show the symptom, not whichever service actually owns the underlying cause. "
              + "Service A's team did nothing wrong -- they investigated their own service thoroughly and "
              + "correctly ruled themselves out. That correct process is exactly what cost 15 minutes."
            : "Unexpected -- investigate.");
    }
}
