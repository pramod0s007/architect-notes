public class ShipmentOrder {
    private final String id;
    private final String customerId;
    private final String customerEmail;
    private final String destinationAddress;
    private final String productSku;
    private final int quantity;

    public ShipmentOrder(String id, String customerId, String customerEmail,
                         String destinationAddress, String productSku, int quantity) {
        this.id = id;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.destinationAddress = destinationAddress;
        this.productSku = productSku;
        this.quantity = quantity;
    }

    public String getId()                 { return id; }
    public String getCustomerId()         { return customerId; }
    public String getCustomerEmail()      { return customerEmail; }
    public String getDestinationAddress() { return destinationAddress; }
    public String getProductSku()         { return productSku; }
    public int    getQuantity()           { return quantity; }
}
