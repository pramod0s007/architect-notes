public class Main {
    static final int MAX_RETRIES = 3;

    // Retries any failing test, up to MAX_RETRIES times, and calls it a pass
    // the moment any single attempt succeeds. It cannot tell "this test's
    // environment is flaky" apart from "this code has a real, intermittent
    // bug that occasionally doesn't manifest."
    static boolean runWithRetries(TestCase test) {
        for (int attempt = 0; attempt < Math.min(MAX_RETRIES, test.attemptResults.length); attempt++) {
            if (test.attemptResults[attempt]) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // TestA: a genuinely flaky test -- its own harness has a real timing
        // issue unrelated to the code under test. Fails once, passes on retry.
        TestCase testA = new TestCase("TestA (flaky harness)", new boolean[]{false, true});

        // TestB: NOT flaky infrastructure -- a real, intermittent race
        // condition in the code under test. It fails most of the time and
        // occasionally passes purely by timing luck.
        TestCase testB = new TestCase("TestB (real race-condition bug)", new boolean[]{false, false, true});

        boolean aPassed = runWithRetries(testA);
        boolean bPassed = runWithRetries(testB);

        System.out.println(testA.name + ": " + (aPassed ? "PASS" : "FAIL") + " (passed on attempt 2 of 2)");
        System.out.println(testB.name + ": " + (bPassed ? "PASS" : "FAIL") + " (passed on attempt 3 of 3)");
        System.out.println();
        System.out.println("Build result: " + (aPassed && bPassed ? "PASS" : "FAIL"));

        System.out.println();
        System.out.println(aPassed && bPassed
            ? "CONFIRMED FAILURE: the build passed and merged, and TestB -- which represents a real, "
              + "dangerous race condition in the code, not a flaky test environment -- passed right "
              + "along with it. Retrying until any attempt succeeds cannot distinguish 'the harness is "
              + "unreliable' from 'the code is unreliable'; it gives a genuine bug exactly as many "
              + "chances to get lucky as it gives a genuine flake."
            : "Unexpected -- investigate.");
    }
}
