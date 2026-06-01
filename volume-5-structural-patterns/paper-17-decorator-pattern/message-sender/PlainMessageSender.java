package decorator.messagesender;

public class PlainMessageSender implements MessageSender {

    @Override
    public void send(String message, String recipient) {
        System.out.printf("[NETWORK] Sending to %s: %s%n", recipient, message);
    }
}
