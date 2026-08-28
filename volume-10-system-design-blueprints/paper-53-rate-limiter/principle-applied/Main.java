public class Main {
    public static void main(String[] args) {
        // capacity=100, refill 100 tokens per 1000ms == 0.1 tokens/ms -- same nominal rate as the pressure demo
        TokenBucket limiter = new TokenBucket(100, 0.1, 0);

        int acceptedNearEndOfWindow = 0;
        for (int i = 0; i < 100; i++) {
            if (limiter.allow(999)) acceptedNearEndOfWindow++; // drains the bucket
        }

        int acceptedStartOfNextWindow = 0;
        for (int i = 0; i < 100; i++) {
            if (limiter.allow(1001)) acceptedStartOfNextWindow++; // only 2ms of refill has happened
        }

        int totalIn2ms = acceptedNearEndOfWindow + acceptedStartOfNextWindow;
        System.out.println("Configured rate: 100 tokens capacity, refill 0.1 tokens/ms (100/sec nominal).");
        System.out.println("Accepted at t=999 (bucket starts full): " + acceptedNearEndOfWindow);
        System.out.println("Accepted at t=1001 (bucket nearly empty, only 0.2 tokens refilled): " + acceptedStartOfNextWindow);
        System.out.println("Total accepted within the same real 2ms span: " + totalIn2ms);
        System.out.println("No boundary to exploit -- the budget is continuous, not reset.");
    }
}
