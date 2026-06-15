// Opt-in capability: only senders with a delivery analytics platform implement this.
public interface AnalyticsSender extends NotificationSender {
    String getAnalytics();
    void exportAnalytics(String format);
}
