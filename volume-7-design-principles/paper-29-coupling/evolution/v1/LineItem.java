class LineItem {
    private final String description;
    private final double adjustment;

    LineItem(String description, double adjustment) {
        this.description = description;
        this.adjustment = adjustment;
    }

    String getDescription() {
        return description;
    }

    double getAdjustment() {
        return adjustment;
    }
}
