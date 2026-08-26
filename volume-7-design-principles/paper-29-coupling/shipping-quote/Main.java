public class Main {
    public static void main(String[] args) {
        ShippingQuoteFat fat = new ShippingQuoteFat();
        fat.weightKg = 4.2;
        fat.destinationZip = "07102";
        fat.customerLoyaltyTier = "gold";   // RateCalculatorFat never reads this
        fat.promoCode = "SPRING10";          // ...or this
        fat.warehouseId = "WH-EAST";          // ...or this

        RateCalculatorFat fatCalc = new RateCalculatorFat();
        System.out.println("Fat quote (needs all 5 fields to construct): " + fatCalc.quote(fat));

        RateCalculator calc = new RateCalculator();
        LabelPrinter printer = new LabelPrinter();
        System.out.println("Data-coupled quote (needs only 2 primitives): " + calc.quote(4.2, "07102"));
        printer.print("07102");
    }
}
