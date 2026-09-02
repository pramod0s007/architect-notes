public class Main {
    public static void main(String[] args) {
        // This specific alert type -- checkout latency -- was mapped to its
        // real, most likely root cause AHEAD OF TIME, during an
        // architecture review, not decided in the moment by which service
        // happens to display the symptom. The routing points at Service B.
        System.out.println("Alert fires: checkout latency high. Routed to: Service B (pre-established owner for this alert type).");
        System.out.println("t=0: Service B on-call -- the team that can actually fix this -- begins investigating.");

        int timeServiceBStarts = 0;

        System.out.println();
        System.out.println("Time until the team that can actually fix the root cause starts working on it: " + timeServiceBStarts + " minutes");

        System.out.println();
        System.out.println(timeServiceBStarts == 0
            ? "FIXED: the correct team starts immediately, at t=0, because ownership for this specific "
              + "alert was decided in advance, based on which service actually owns the most likely root "
              + "cause -- not decided during the incident, based on which service happens to display the "
              + "symptom. No time was spent ruling anyone out, because the right team was never in doubt."
            : "Unexpected -- investigate.");
    }
}
