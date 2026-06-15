// v1: Small, focused interface. Two methods, both relevant to every sender.
// No ISP pressure yet — one implementation, nothing being forced to fake.
public interface NotificationSender {
    void send(String recipient, String message);
    DeliveryStatus getDeliveryStatus(String id);
}
