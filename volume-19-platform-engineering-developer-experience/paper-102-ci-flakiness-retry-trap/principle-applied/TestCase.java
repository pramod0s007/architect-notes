class TestCase {
    final String name;
    final boolean[] attemptResults; // recorded outcome of each real attempt, in order

    TestCase(String name, boolean[] attemptResults) {
        this.name = name;
        this.attemptResults = attemptResults;
    }
}
