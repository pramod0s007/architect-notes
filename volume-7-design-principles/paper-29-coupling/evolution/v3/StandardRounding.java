class StandardRounding implements TaxRoundingPolicy {
    public double apply(double total) {
        return Math.round(total * 100) / 100.0;
    }
}
