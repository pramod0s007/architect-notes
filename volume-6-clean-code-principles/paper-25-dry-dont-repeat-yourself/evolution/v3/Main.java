public class Main {

    public static void main(String[] args) {
        DiscountCalculator calc = new DiscountCalculator();

        CartService cart           = new CartService(calc);
        InvoiceGenerator invoicer  = new InvoiceGenerator(calc);
        RefundService refund        = new RefundService(calc);

        Order loyalCustomer  = new Order("O1", 200.0, 5,  false, "C1"); // 5% discount
        Order promoUser      = new Order("O2", 100.0, 0,  true,  "C2"); // 10% promo
        Order highTierUser   = new Order("O3", 500.0, 40, false, "C3"); // capped at 30%

        System.out.println("--- Cart ---");
        System.out.println(cart.calculateTotal(loyalCustomer));  // 190.0
        System.out.println(cart.calculateTotal(promoUser));      // 90.0
        System.out.println(cart.calculateTotal(highTierUser));   // 350.0 (capped)

        System.out.println("--- Invoice ---");
        System.out.println(invoicer.generate(loyalCustomer));
        System.out.println(invoicer.generate(highTierUser));

        System.out.println("--- Refund ---");
        System.out.println(refund.calculateRefund(loyalCustomer));  // matches cart — no drift
        System.out.println(refund.calculateRefund(highTierUser));   // 350.0 — bug impossible now
    }
}
