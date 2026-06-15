public class Main {

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Tax Calculator — OCP Demo");
        System.out.println(" Open for extension, closed for modification");
        System.out.println("══════════════════════════════════════════\n");

        TaxCalculator calculator = new TaxCalculator();

        System.out.println("\n--- Purchase amount: 1000.00 ---");
        calculator.compute("US", 1000.00);
        calculator.compute("UK", 1000.00);
        calculator.compute("DE", 1000.00);
        calculator.compute("IN", 1000.00);

        System.out.println("\n--- India essentials (amount < 500): 5% GST ---");
        calculator.compute("IN", 300.00);

        System.out.println("\n--- High-value purchase: 50000.00 ---");
        calculator.compute("US", 50000.00);
        calculator.compute("DE", 50000.00);

        System.out.println("\n--- Unregistered country triggers exception ---");
        try {
            calculator.compute("BR", 1000.00);
        } catch (IllegalArgumentException e) {
            System.out.println("[Main] Caught expected error: " + e.getMessage());
            System.out.println("[Main] Fix: create BrazilTaxRule, call calculator.register(new BrazilTaxRule())");
        }
    }
}
