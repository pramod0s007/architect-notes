class TimestampedMessage {
    final long clientTimestamp; // set by the SENDING DEVICE's own clock
    final String text;

    TimestampedMessage(long clientTimestamp, String text) {
        this.clientTimestamp = clientTimestamp;
        this.text = text;
    }
}
