// Common coupling: two unrelated services read and write the same static mutable field.
class AppConfig {
    static double currentDiscountRate = 0.0;
}
