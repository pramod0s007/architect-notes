public class Main {
    public static void main(String[] args) {
        PaymentRouter paymentRouter = new PaymentRouter();
        Order order = new Order("ORD-1");

        long start = System.currentTimeMillis();
        boolean charged = paymentRouter.charge(order);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("v1: one order, one dependency, no shared pool to contend for.");
        System.out.println("    charged=" + charged + ", took " + elapsed + "ms (3 retries against a dead backend).");
        System.out.println("    Retrying inline is harmless here -- nothing else is waiting on this thread.");
    }
}
