// YAGNI violation: built for 'future discount types' that don't exist yet.
// One concrete type. Registry + factory = unnecessary complexity.
public class DiscountRuleFactory {

    public DiscountRule create(String type) {
        switch (type) {
            case "loyalty":
                return new LoyaltyDiscountRule();
            default:
                // Placeholder for types we imagined but haven't built.
                throw new UnsupportedOperationException("Unknown discount type: " + type);
        }
    }
}
