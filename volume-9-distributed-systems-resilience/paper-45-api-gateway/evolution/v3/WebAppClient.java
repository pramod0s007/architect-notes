class WebAppClient {
    private final ApiGateway apiGateway;

    WebAppClient(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    String placeOrder(Order order) {
        return apiGateway.placeOrder(order);
    }
}
