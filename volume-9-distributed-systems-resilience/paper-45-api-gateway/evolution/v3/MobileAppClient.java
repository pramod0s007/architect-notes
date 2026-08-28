// Every client, regardless of platform, calls exactly one thing.
class MobileAppClient {
    private final ApiGateway apiGateway;

    MobileAppClient(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    String placeOrder(Order order) {
        return apiGateway.placeOrder(order);
    }
}
