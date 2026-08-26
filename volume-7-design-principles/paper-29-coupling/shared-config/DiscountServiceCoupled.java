class DiscountServiceCoupled {
    void startFlashSale() {
        AppConfig.currentDiscountRate = 0.20; // 20% flash sale
    }

    double priceOf(double amount) {
        return amount * (1 - AppConfig.currentDiscountRate);
    }
}
