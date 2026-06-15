// 5 configurable weights tuned by hand with no data to back them up.
public class RelevanceScorer {

    private final double titleWeight;
    private final double descriptionWeight;
    private final double categoryWeight;
    private final double recencyWeight;
    private final double popularityWeight;

    public RelevanceScorer(double titleWeight, double descriptionWeight,
                           double categoryWeight, double recencyWeight,
                           double popularityWeight) {
        this.titleWeight       = titleWeight;
        this.descriptionWeight = descriptionWeight;
        this.categoryWeight    = categoryWeight;
        this.recencyWeight     = recencyWeight;
        this.popularityWeight  = popularityWeight;
    }

    public double score(Product product, String query) {
        String q    = query.toLowerCase();
        double score = 0;

        if (product.name.toLowerCase().contains(q))     score += titleWeight;
        if (product.category.toLowerCase().contains(q)) score += categoryWeight;

        // descriptionWeight, recencyWeight, popularityWeight have no data source.
        // Included because "we might need them later."
        score += recencyWeight * 0;    // no recency data
        score += popularityWeight * 0; // no popularity data

        return score;
    }
}
