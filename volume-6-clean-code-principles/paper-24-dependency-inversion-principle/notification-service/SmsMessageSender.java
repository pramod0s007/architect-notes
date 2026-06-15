/**
 * Low-level detail: SMS delivery.
 * AlertService never imports or knows about this class.
 */
public class SmsMessageSender implements MessageSender {

    @Override
    public void send(String recipient, String message) {
        System.out.println("SMS → " + recipient + ": " + message);
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
