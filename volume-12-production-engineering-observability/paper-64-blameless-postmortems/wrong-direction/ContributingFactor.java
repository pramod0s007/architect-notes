class ContributingFactor {
    final String description;
    final boolean critical; // would this factor, left unaddressed, plausibly cause a recurrence?
    boolean remediated = false;

    ContributingFactor(String description, boolean critical) {
        this.description = description;
        this.critical = critical;
    }
}
