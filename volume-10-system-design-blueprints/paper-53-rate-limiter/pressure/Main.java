public class Main {
    public static void main(String[] args) {
        // limit=100 per 1000ms window
        FixedWindowCounter limiter = new FixedWindowCounter(100, 1000);

        int acceptedNearEndOfWindow = 0;
        for (int i = 0; i < 100; i++) {
            if (limiter.allow(999)) acceptedNearEndOfWindow++; // all still inside window [0, 1000)
        }

        int acceptedStartOfNextWindow = 0;
        for (int i = 0; i < 100; i++) {
            if (limiter.allow(1001)) acceptedStartOfNextWindow++; // window [1000, 2000) just started
        }

        int totalIn2ms = acceptedNearEndOfWindow + acceptedStartOfNextWindow;
        System.out.println("Configured limit: 100 requests per 1000ms.");
        System.out.println("Accepted at t=999 (end of window 1): " + acceptedNearEndOfWindow);
        System.out.println("Accepted at t=1001 (start of window 2): " + acceptedStartOfNextWindow);
        System.out.println("Total accepted within a real 2ms span: " + totalIn2ms);
        System.out.println("That's " + totalIn2ms + " requests in ~2ms against a \"100 per second\" limit.");
    }
}
