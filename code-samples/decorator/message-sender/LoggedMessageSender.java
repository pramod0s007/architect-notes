package decorator.messagesender;

import java.time.Instant;

public class LoggedMessageSender implements MessageSender {

    private final MessageSender wrapped;

    public LoggedMessageSender(MessageSender wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message, String recipient) {
        System.out.printf("[LOG] %s — Sending to %s (len=%d)%n",
            Instant.now(), recipient, message.length());
        wrapped.send(message, recipient);
        System.out.printf("[LOG] %s — Send complete%n", Instant.now());
    }
}
