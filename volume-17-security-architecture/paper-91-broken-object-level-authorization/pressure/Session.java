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
