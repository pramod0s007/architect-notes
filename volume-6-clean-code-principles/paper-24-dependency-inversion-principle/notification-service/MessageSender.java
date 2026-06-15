/**
 * DIP: High-level abstraction for sending messages.
 * AlertService depends on this interface — never on concrete senders.
 */
public interface MessageSender {

    void send(String recipient, String message);

    String getChannelName();
}
