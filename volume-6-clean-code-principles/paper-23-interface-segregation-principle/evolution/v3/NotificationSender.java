// v3: ISP applied. Base interface is minimal — only what every sender can do.
// Capabilities are composed via narrow sub-interfaces, not crammed into one fat interface.
public interface NotificationSender {
    void send(String recipient, String message);
    DeliveryStatus getDeliveryStatus(String id);
}
