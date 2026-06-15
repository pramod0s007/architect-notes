public class EmailSender implements NotificationSender {

    @Override
    public void send(String recipient, String message) {
        System.out.println("Email sent to " + recipient + ": " + message);
    }

    @Override
    public DeliveryStatus getDeliveryStatus(String id) {
        return new DeliveryStatus(id, "DELIVERED");
    }

    public static void main(String[] args) {
        NotificationSender sender = new EmailSender();
        sender.send("user@example.com", "Welcome!");
        System.out.println(sender.getDeliveryStatus("msg-001"));
    }
}
