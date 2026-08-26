public class Main {
    public static void main(String[] args) {
        DiscountServiceCoupled discountService = new DiscountServiceCoupled();
        PromotionServiceCoupled promoService = new PromotionServiceCoupled();

        discountService.startFlashSale();
        promoService.applyNewCustomerPromo(); // silently overwrites the flash-sale rate

        System.out.println("Coupled: price during 'flash sale' is actually "
                + discountService.priceOf(100.0) + " (wrong rate, clobbered by promo service)");

        DiscountService fixed = new DiscountService();
        System.out.println("Fixed flash-sale price:      " + fixed.priceOf(100.0, 0.20));
        System.out.println("Fixed new-customer price:    " + fixed.priceOf(100.0, 0.05));
    }
}
