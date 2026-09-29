/**
 * PRESSURE: three concurrent API calls each discover their access token has expired and each
 * independently triggers its own token refresh, with no coordination between them. The server uses
 * rotating, single-use refresh tokens (a common real-world security practice): each refresh token is
 * valid for exactly one refresh call, after which the server invalidates it and issues a new one. This
 * program is single-threaded and sequential, not multi-threaded -- it snapshots the refresh token all
 * three modeled callers would have observed before any of them acted, then invokes their refresh
 * attempts one after another purely to make the server's deterministic acceptance order visible, not to
 * simulate real threads. In this modeled server's policy, reuse of an already-rotated token is simply
 * rejected (some real authorization servers instead treat reuse as replay evidence and revoke the whole
 * token family, which would make this race even more damaging than what's shown here). The first call
 * to refresh wins and rotates the token; the second and third calls, still holding the now-already-used
 * refresh token, get rejected by the server -- not because anything was actually wrong with the user's
 * session, but because nothing told them another call had already refreshed it for them.
 */
public class Main {
    private static String currentRefreshToken = "refresh-token-v1";

    public static void main(String[] args) {
        String tokenAllThreeCallsObserved = currentRefreshToken;
        System.out.println("Access token has expired. Three concurrent calls each discover this at the same "
                + "moment and each independently attempts to refresh, all using the refresh token they each "
                + "observed before any of them acted: " + tokenAllThreeCallsObserved);

        String call1Result = refreshToken("Call 1", tokenAllThreeCallsObserved);
        String call2Result = refreshToken("Call 2", tokenAllThreeCallsObserved);
        String call3Result = refreshToken("Call 3", tokenAllThreeCallsObserved);

        System.out.println("Call 1 outcome: " + call1Result);
        System.out.println("Call 2 outcome: " + call2Result);
        System.out.println("Call 3 outcome: " + call3Result);

        System.out.println("Result: only the first call to reach the server succeeded in refreshing -- "
                + "the second and third calls were rejected for using an already-rotated refresh token, "
                + "even though, under this modeled server's reuse policy, the user's session would "
                + "otherwise have remained perfectly valid, having just been refreshed a moment earlier "
                + "by Call 1.");
    }

    private static String refreshToken(String caller, String tokenUsed) {
        if (tokenUsed.equals(currentRefreshToken)) {
            currentRefreshToken = currentRefreshToken.equals("refresh-token-v1")
                    ? "refresh-token-v2" : "refresh-token-v3";
            return caller + ": refresh SUCCEEDED, new access token issued, refresh token rotated to "
                    + currentRefreshToken;
        } else {
            return caller + ": refresh REJECTED -- refresh token " + tokenUsed
                    + " was already used and rotated by a prior call";
        }
    }
}
