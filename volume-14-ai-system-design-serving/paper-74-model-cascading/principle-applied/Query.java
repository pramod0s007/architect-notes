// A query, its difficulty category, the cheap model's self-reported confidence
// on it, and whether the cheap model's answer was ACTUALLY correct (ground
// truth, verified by a human evaluator -- used only to check outcomes, never
// fed into any routing decision itself).
class Query {
    final String id;
    final String category;
    final double cheapModelConfidence;
    final boolean cheapModelActuallyCorrect;

    Query(String id, String category, double cheapModelConfidence, boolean cheapModelActuallyCorrect) {
        this.id = id;
        this.category = category;
        this.cheapModelConfidence = cheapModelConfidence;
        this.cheapModelActuallyCorrect = cheapModelActuallyCorrect;
    }
}
