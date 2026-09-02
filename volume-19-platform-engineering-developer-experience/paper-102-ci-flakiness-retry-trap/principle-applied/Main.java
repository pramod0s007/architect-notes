import java.util.Set;

public class Main {
    static final int MAX_RETRIES = 3;
    // A documented, tracked allowlist -- someone has looked at this specific
    // test, confirmed the flake is in the harness, and is accountable for
    // fixing it. Nothing gets on this list by default.
    static final Set<String> knownFlakyTests = Set.of("TestA (flaky harness)");

    static boolean runTest(TestCase test) {
        boolean retriesAllowed = knownFlakyTests.contains(test.name);
        int attemptsToTry = retriesAllowed ? Math.min(MAX_RETRIES, test.attemptResults.length) : 1;
        for (int attempt = 0; attempt < attemptsToTry; attempt++) {
            if (test.attemptResults[attempt]) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        TestCase testA = new TestCase("TestA (flaky harness)", new boolean[]{false, true});
        TestCase testB = new TestCase("TestB (real race-condition bug)", new boolean[]{false, false, true});

        boolean aPassed = runTest(testA);
        boolean bPassed = runTest(testB);

        System.out.println(testA.name + ": " + (aPassed ? "PASS" : "FAIL") + " (tagged known-flaky, retried, passed on attempt 2)");
        System.out.println(testB.name + ": " + (bPassed ? "PASS" : "FAIL") + " (not tagged, one attempt only, failed)");
        System.out.println();
        System.out.println("Build result: " + (aPassed && bPassed ? "PASS" : "FAIL"));

        System.out.println();
        System.out.println(!bPassed
            ? "FIXED: TestA, a documented and tracked flake, still passes after its allowed retry -- "
              + "nothing about legitimate flakiness got harder. TestB, which was never tagged as flaky "
              + "because it isn't flaky, gets exactly one attempt like every other untagged test, fails "
              + "on it, and blocks the build before the real race condition ever reaches production."
            : "Unexpected -- investigate.");
    }
}
