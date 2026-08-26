class NoRounding implements TaxRoundingPolicy {
    public double apply(double total) {
        return total;
    }
}
