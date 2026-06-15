public class DeliveryStatus {
    private final String id;
    private final String status;

    public DeliveryStatus(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return "DeliveryStatus{id='" + id + "', status='" + status + "'}";
    }
}
