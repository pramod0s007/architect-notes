public class Main {
    static final int WRONG_ROUTING_WASTED_MINUTES = 15; // the pressure case's cost, for comparison
    // A documented, real effect of paging multiple responders for one
    // incident: each assumes someone else already started, and waits to
    // confirm before acting themselves, rather than beginning immediately.
    static final int DIFFUSION_OF_RESPONSIBILITY_DELAY_MINUTES = 20;

    public static void main(String[] args) {
        // Overcorrection: nobody has to guess which team owns this anymore
        // -- every team that could conceivably be involved gets paged
        // simultaneously, Service A AND Service B both. That part
        // genuinely solves "did the right team get notified."
        System.out.println("Alert fires: checkout latency high. Routed to: Service A AND Service B, simultaneously.");
        System.out.println("Both teams see multiple responders paged for the same incident.");

        int timeServiceBActuallyStarts = DIFFUSION_OF_RESPONSIBILITY_DELAY_MINUTES;
        System.out.println("t=0: both on-calls are paged, both see the other team was also paged.");
        System.out.println("t=" + timeServiceBActuallyStarts + ": Service B -- the team that can actually fix this -- "
            + "actually begins investigating, having first waited to see if the other paged team would act.");

        System.out.println();
        System.out.println("Time until the team that can actually fix the root cause starts working on it: "
            + timeServiceBActuallyStarts + " minutes");
        System.out.println("For comparison, the narrow-but-wrong routing from the pressure case cost: "
            + WRONG_ROUTING_WASTED_MINUTES + " minutes");

        System.out.println();
        System.out.println(timeServiceBActuallyStarts > WRONG_ROUTING_WASTED_MINUTES
            ? "CONFIRMED FAILURE: paging every team that could conceivably be involved was slower than "
              + "paging only the WRONG team. Service B -- the correct owner, notified immediately and "
              + "directly this time -- still didn't start real work until minute " + timeServiceBActuallyStarts
              + ", because being one of several people paged for the same incident is itself a delay: "
              + "each responder has a rational reason to assume someone else is already on it. Broader "
              + "notification solved 'was the right team told' and quietly introduced a new, larger "
              + "delay in its place."
            : "Unexpected -- investigate.");
    }
}
