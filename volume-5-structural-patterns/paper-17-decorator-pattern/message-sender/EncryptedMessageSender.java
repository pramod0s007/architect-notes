package decorator.messagesender;

public class EncryptedMessageSender implements MessageSender {

    private final MessageSender wrapped;

    public EncryptedMessageSender(MessageSender wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message, String recipient) {
        String encrypted = encrypt(message);
        System.out.printf("[ENCRYPT] Message encrypted (length %d)%n", encrypted.length());
        wrapped.send(encrypted, recipient);
    }

    private String encrypt(String message) {
        // Simulate encryption
        return "ENC[" + new StringBuilder(message).reverse() + "]";
    }
}
