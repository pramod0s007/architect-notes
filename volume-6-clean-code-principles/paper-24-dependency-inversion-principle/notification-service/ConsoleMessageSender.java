/**
 * Test double: console sender that also remembers what it sent.
 * Demonstrates DIP — tests supply a different implementation without touching AlertService.
 */
public class ConsoleMessageSender implements MessageSender {

    private String lastMessage;
    private String lastRecipient;

    @Override
    public void send(String recipient, String message) {
        this.lastRecipient = recipient;
        this.lastMessage = message;
        System.out.println("[CONSOLE] To=" + recipient + " | " + message);
    }

    @Override
    public String getChannelName() {
        return "Console";
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastRecipient() {
        return lastRecipient;
    }
}
