class GatewayRequest {
    final int id;
    final long startTimeMs;
    final long endTimeMs;

    GatewayRequest(int id, long startTimeMs, long endTimeMs) {
        this.id = id;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    boolean wasActiveAt(long timeMs) {
        return startTimeMs <= timeMs && timeMs <= endTimeMs;
    }
}
