package decorator.messagesender;

public class CompressedMessageSender implements MessageSender {

    private final MessageSender wrapped;

    public CompressedMessageSender(MessageSender wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message, String recipient) {
        String compressed = compress(message);
        System.out.printf("[COMPRESS] %d → %d bytes%n", message.length(), compressed.length());
        wrapped.send(compressed, recipient);
    }

    private String compress(String message) {
        // Simulate compression
        return message.length() > 10 ? message.substring(0, message.length() / 2) + "..." : message;
    }
}
