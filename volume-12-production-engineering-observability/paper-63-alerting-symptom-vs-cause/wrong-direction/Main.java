public class Main {
    public static void main(String[] args) {
        InternalLatencyAlerter internalAlerter = new InternalLatencyAlerter();
        EndToEndLatencyAlerter endToEndAlerter = new EndToEndLatencyAlerter();

        // The load balancer's queue backs up -- requests wait 3000ms before
        // an app server even starts on them. Once a request IS being
        // processed, the app server itself is still fast: 50ms.
        double queueTimeMs = 3000;
        double internalProcessingTimeMs = 50;

        boolean internalAlert = internalAlerter.shouldAlert(internalProcessingTimeMs);
        boolean endToEndAlert = endToEndAlerter.shouldAlert(queueTimeMs, internalProcessingTimeMs);

        System.out.println("Queue wait: " + queueTimeMs + "ms, internal processing: " + internalProcessingTimeMs + "ms");
        System.out.println("Internal-latency alerter (measures processing time only): alert=" + internalAlert
                + " (WRONG -- customers waited 3050ms total)");
        System.out.println("End-to-end alerter (measures queue + processing): alert=" + endToEndAlert
                + " (CORRECT -- this is what the customer actually experienced)");
    }
}
