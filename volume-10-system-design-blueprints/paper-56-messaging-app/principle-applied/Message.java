class Message {
    final int sequenceNumber; // assigned once, by the server, when the message is sent
    final String text;

    Message(int sequenceNumber, String text) {
        this.sequenceNumber = sequenceNumber;
        this.text = text;
    }
}
