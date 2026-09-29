/**
 * PRINCIPLE APPLIED: singleflight. When a call discovers its access token has expired, it checks
 * whether a refresh is already pending. If none is, it registers itself as the owner of a new pending
 * refresh (the check-and-register step has to be atomic against the same key in a real implementation --
 * a plain "if null, then set" is only safe here because this program is single-threaded and sequential,
 * not because the logic alone would be safe under real concurrency). If a refresh is already pending,
 * the call joins it instead of starting its own or giving up -- it doesn't get a result yet, because the
 * refresh hasn't finished. Only after every caller has either started or joined the one pending refresh
 * does that refresh actually complete, at which point every caller -- the one who started it and the two
 * who joined it -- retries its original request with the result it produced. Only one refresh call is
 * ever made to the server, and every concurrent caller still gets a successful outcome, because none of
 * them are racing each other or abandoning each other -- they're all waiting on the same pending work.
 */
public class Main {
    private static String currentRefreshToken = "refresh-token-v1";
    private static boolean refreshIsPending = false;
    private static String resolvedNewAccessToken = null;

    public static void main(String[] args) {
        System.out.println("Access token has expired. Three concurrent calls each discover this at the "
                + "same moment, before any refresh has started or finished.");

        System.out.println("Call 1 checks: no refresh is pending -- registers itself as the owner of a "
                + "new pending refresh. Not resolved yet.");
        refreshIsPending = true;

        System.out.println("Call 2 checks: a refresh is already pending (owned by Call 1) -- joins it "
                + "instead of starting its own or giving up. Not resolved yet, so Call 2 waits.");

        System.out.println("Call 3 checks: a refresh is already pending -- joins it too. Also waits.");

        System.out.println("Only now does the pending refresh actually complete against the server, on "
                + "Call 1's behalf as the owner.");
        resolvedNewAccessToken = performRefresh(currentRefreshToken);
        refreshIsPending = false;
        System.out.println("Pending refresh resolved: " + resolvedNewAccessToken);

        System.out.println("Call 1 retries its original request with access token: " + resolvedNewAccessToken
                + " -- SUCCEEDED");
        System.out.println("Call 2 retries its original request with access token: " + resolvedNewAccessToken
                + " -- SUCCEEDED");
        System.out.println("Call 3 retries its original request with access token: " + resolvedNewAccessToken
                + " -- SUCCEEDED");

        System.out.println("Result: exactly one refresh call was made to the server, and all three "
                + "concurrent calls succeeded -- neither the pressure scenario's wasted, rejected refresh "
                + "attempts nor the wrong-direction scenario's dropped requests happened, because Call 2 "
                + "and Call 3 joined the one pending refresh while it was still unresolved, instead of "
                + "racing it or abandoning it.");
    }

    private static String performRefresh(String tokenUsed) {
        currentRefreshToken = "refresh-token-v2";
        return "new-access-token-from-" + tokenUsed;
    }
}
