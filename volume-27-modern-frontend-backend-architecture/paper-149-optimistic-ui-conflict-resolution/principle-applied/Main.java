/**
 * PRINCIPLE APPLIED: each optimistic request is tagged with a monotonically increasing sequence
 * number when it's sent. When a response arrives, the client only applies it if its sequence number
 * is higher than the highest one already applied -- a response that arrives out of order, carrying
 * an older sequence number than one already applied, is recognized as stale and discarded instead of
 * being allowed to overwrite a newer, already-correct value. Same out-of-order arrival as the
 * wrong-direction scenario: request #2's response (sequence 2, up-to-date) still arrives first,
 * and request #1's response (sequence 1, stale) still arrives second -- but this time the stale one
 * is rejected instead of applied.
 */
public class Main {
    private static final int BASE_COUNT = 10;

    private static int highestAppliedSequence = 0;

    public static void main(String[] args) {
        System.out.println("Base server count: " + BASE_COUNT);

        int localCount = BASE_COUNT;
        localCount += 1;
        System.out.println("User clicks like (1st time, sequence 1) -- optimistic local count: " + localCount);
        localCount += 1;
        System.out.println("User clicks like (2nd time, sequence 2) -- optimistic local count: " + localCount);

        int responseTwoCount = BASE_COUNT + 1 + 1 + 1; // request 1 + concurrent external like + request 2
        localCount = applyResponse(localCount, 2, responseTwoCount);

        int responseOneCount = BASE_COUNT + 1; // request 1 only, a stale snapshot
        localCount = applyResponse(localCount, 1, responseOneCount);

        System.out.println("Final displayed count: " + localCount);
        System.out.println("Actual current server count: " + responseTwoCount);

        if (localCount == responseTwoCount) {
            System.out.println("Result: the correct count (" + responseTwoCount + ") was applied once and never "
                    + "overwritten by the later-arriving, lower-sequence stale response -- sequence-based "
                    + "rejection kept the display converged on the newest response this client had accepted.");
        }
    }

    private static int applyResponse(int currentLocalCount, int sequence, int serverCount) {
        if (sequence > highestAppliedSequence) {
            highestAppliedSequence = sequence;
            System.out.println("Response for request sequence " + sequence + " arrives: server count = "
                    + serverCount + " -- sequence " + sequence + " > highest applied so far, APPLIED");
            return serverCount;
        } else {
            System.out.println("Response for request sequence " + sequence + " arrives: server count = "
                    + serverCount + " -- sequence " + sequence + " <= highest applied (" + highestAppliedSequence
                    + "), DISCARDED as stale/out-of-order");
            return currentLocalCount;
        }
    }
}
