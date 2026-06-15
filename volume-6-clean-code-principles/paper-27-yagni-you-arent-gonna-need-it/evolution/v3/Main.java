import java.util.List;

public class Main {

    public static void main(String[] args) {
        DiscountCalculator calculator = new DiscountCalculator(
            List.of(new LoyaltyDiscountRule(), new PromoCodeDiscountRule())
        );

        Order loyalOnly   = new Order("O1", 200.0, 3,  false); // loyalty: min(3×10%, 30%) = 30% → 30% of 200 = 60.0
        Order promoOnly   = new Order("O2", 100.0, 0,  true);  // promo: 15
        Order both        = new Order("O3", 200.0, 3,  true);  // loyalty 30 + promo 30 = 60, cap at 60
        Order highLoyalty = new Order("O4", 500.0, 10, false); // 10*10=100% → capped at 30% = 150

        System.out.println("Loyal only discount:    " + calculator.calculate(loyalOnly));   // 60.0
        System.out.println("Promo only discount:    " + calculator.calculate(promoOnly));   // 15.0
        System.out.println("Both applied (capped):  " + calculator.calculate(both));        // 60.0
        System.out.println("High loyalty (capped):  " + calculator.calculate(highLoyalty)); // 150.0
    }
}
