/**
 * Low-level detail: Slack delivery.
 * Plugged in via constructor injection — no changes to AlertService required.
 */
public class SlackMessageSender implements MessageSender {

    private final String channelName;

    public SlackMessageSender(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public void send(String recipient, String message) {
        System.out.println("Slack #" + channelName + " → " + message);
    }

    @Override
    public String getChannelName() {
        return "Slack";
    }
}
