// A session proves WHO is making a request (authentication) -- it says
// nothing on its own about WHAT that identity is allowed to access
// (authorization). authenticatedUserId is server-issued at login and cannot
// be influenced by the request itself.
class Session {
    final String sessionId;
    final String authenticatedUserId;
    final boolean valid;

    Session(String sessionId, String authenticatedUserId, boolean valid) {
        this.sessionId = sessionId;
        this.authenticatedUserId = authenticatedUserId;
        this.valid = valid;
    }
}
