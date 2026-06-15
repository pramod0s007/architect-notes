// v3: The Open/Closed Principle applied.
// FeeCalculator is now closed for modification, open for extension.
// Adding a new payment method = adding a new class. Zero changes to existing code.
public interface FeeStrategy {
    double calculate(double amount);
}
