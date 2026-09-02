class Session {
    final String sessionId;
    final String authenticatedUserId; // exactly as this account registered, e.g. "u1"
    final boolean valid;

    Session(String sessionId, String authenticatedUserId, boolean valid) {
        this.sessionId = sessionId;
        this.authenticatedUserId = authenticatedUserId;
        this.valid = valid;
    }
}
