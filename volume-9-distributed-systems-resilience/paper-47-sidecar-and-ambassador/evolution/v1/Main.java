public class Main {
    public static void main(String[] args) {
        SinglePaymentService payment = new SinglePaymentService();
        Order order = new Order("ORD-1");

        System.out.println("Charged: " + payment.charge(order));
        System.out.println("v1: one service, one retry policy, inline. Nothing to keep consistent yet.");
    }
}
