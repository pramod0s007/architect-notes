public class Main {
    // Overcorrection: retries are bounded now -- this isn't the pressure
    // case's unlimited-per-test-until-pass mistake. But the budget is
    // shared across the WHOLE build, drawn down by whichever test needs
    // it, regardless of which specific test is doing the drawing.
    static int sharedRetryBudget = 3;

    static boolean runTest(TestCase test) {
        for (int attempt = 0; attempt < test.attemptResults.length; attempt++) {
            if (test.attemptResults[attempt]) {
                return true;
            }
            if (attempt < test.attemptResults.length - 1) {
                if (sharedRetryBudget <= 0) {
                    return false; // out of budget, no more retries for anyone
                }
                sharedRetryBudget--;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        TestCase testA = new TestCase("TestA (flaky harness)", new boolean[]{false, true});
        TestCase testB = new TestCase("TestB (real race-condition bug)", new boolean[]{false, false, true});

        boolean aPassed = runTest(testA);
        System.out.println(testA.name + ": " + (aPassed ? "PASS" : "FAIL") + ", shared retry budget remaining: " + sharedRetryBudget);

        boolean bPassed = runTest(testB);
        System.out.println(testB.name + ": " + (bPassed ? "PASS" : "FAIL") + ", shared retry budget remaining: " + sharedRetryBudget);

        System.out.println();
        System.out.println("Build result: " + (aPassed && bPassed ? "PASS" : "FAIL"));

        System.out.println();
        System.out.println(aPassed && bPassed
            ? "CONFIRMED FAILURE: TestB -- the real race-condition bug -- borrowed retries from the same "
              + "shared pool meant for TestA's genuine, documented flake, and used them to get lucky on "
              + "its third attempt, exactly as it did with no retry policy at all. A budget that only "
              + "counts HOW MANY retries have been used, without tracking WHICH specific tests are "
              + "known, tracked flakes, still lets an untracked, real bug spend the same currency as a "
              + "documented one."
            : "Unexpected -- investigate.");
    }
}
