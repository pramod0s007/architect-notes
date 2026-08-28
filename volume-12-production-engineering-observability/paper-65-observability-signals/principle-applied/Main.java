public class Main {
    public static void main(String[] args) {
        Trace trace = new Trace();
        trace.addSpan("Gateway", 10);
        trace.addSpan("ServiceA", 25);
        trace.addSpan("ServiceB", 15);
        trace.addSpan("ServiceC", 3000); // the same hang from the pressure scenario

        Span slowest = trace.slowestSpan();

        System.out.println("Total request duration: " + trace.totalDurationMs() + "ms");
        System.out.println("Slowest span: " + slowest.serviceName + " (" + slowest.durationMs + "ms)");
        System.out.println("The trace answers the exact question metrics alone could not: ServiceC caused this.");
    }
}
