/**
 * WRONG DIRECTION: overcorrecting from "no coordination at all" to a naive guard flag: if a refresh is
 * already in progress, any other call that discovers the expired token simply gives up immediately
 * instead of waiting for that in-progress refresh to finish. This does stop the multiple-refresh-calls
 * problem -- only one refresh ever actually happens -- but it does so by sacrificing every other
 * concurrent call outright: Call 2 and Call 3 never even get a chance to succeed once Call 1's refresh
 * completes, because they already bailed out the moment they saw the flag set, instead of queuing
 * behind it. The staging below is deliberately explicit about *when* each call checks the flag relative
 * to when Call 1's refresh actually finishes, since a real concurrent interleaving isn't something a
 * single-threaded, sequential program can otherwise demonstrate.
 */
public class Main {
    private static String currentRefreshToken = "refresh-token-v1";
    private static boolean isRefreshing = false;

    public static void main(String[] args) {
        System.out.println("Access token has expired. Three concurrent calls each discover this at the "
                + "same moment, before Call 1's refresh has had a chance to finish.");

        System.out.println("Call 1 checks the flag: not set -- starts a refresh, sets the flag.");
        isRefreshing = true;

        System.out.println("Call 2 checks the flag: set by Call 1 -- gives up immediately instead of waiting.");
        String call2Result = "Call 2: FAILED -- another refresh was already in progress, gave up without waiting";

        System.out.println("Call 3 checks the flag: still set -- gives up immediately instead of waiting.");
        String call3Result = "Call 3: FAILED -- another refresh was already in progress, gave up without waiting";

        System.out.println("Only now does Call 1's refresh actually complete against the server.");
        String call1Result = "Call 1: " + refreshToken(currentRefreshToken);
        isRefreshing = false;

        System.out.println("Call 1 outcome: " + call1Result);
        System.out.println("Call 2 outcome: " + call2Result);
        System.out.println("Call 3 outcome: " + call3Result);

        System.out.println("Result: the refresh-token-rotation problem from the pressure scenario is gone "
                + "-- only one refresh call is ever made -- but Call 2 and Call 3 both failed anyway, even "
                + "though Call 1's refresh succeeded moments later and a valid, fresh access token existed "
                + "by the time they had already given up. Avoiding one bug introduced a different one: "
                + "legitimate concurrent requests are being dropped instead of served.");
    }

    private static String refreshToken(String tokenUsed) {
        if (tokenUsed.equals(currentRefreshToken)) {
            currentRefreshToken = "refresh-token-v2";
            return "refresh SUCCEEDED, new access token issued, refresh token rotated to "
                    + currentRefreshToken;
        } else {
            return "refresh REJECTED -- refresh token " + tokenUsed + " was already used and rotated";
        }
    }
}
