public class Main {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        RefundService refundService = new RefundService();

        String customerId = "CUST-EU"; // matches the NEW rule (ends with "-EU"), not the OLD one

        orderService.charge(customerId, 40.0);   // routes to [modern] -- updated rule
        refundService.refund(customerId, 40.0);  // routes to [legacy] -- stale rule, same customer

        System.out.println("Same customer, same routing DECISION supposedly -- charge and refund");
        System.out.println("landed on two different providers, because the rule lives in two places.");
    }
}
