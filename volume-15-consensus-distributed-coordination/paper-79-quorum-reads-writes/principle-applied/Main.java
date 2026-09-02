import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Replica r1 = new Replica("R1", "v1", 1);
        Replica r2 = new Replica("R2", "v1", 1);
        Replica r3 = new Replica("R3", "v1", 1);

        int N = 3, W = 2, R = 2;
        System.out.println("N=" + N + ", W=" + W + ", R=" + R + " (W+R=" + (W + R) + " > N=" + N + ")");

        // Write is acknowledged after W=2 replicas confirm -- R1 and R2. R3 is
        // never told about this write.
        r1.value = "v2"; r1.version = 2;
        r2.value = "v2"; r2.version = 2;
        System.out.println("Write \"v2\" acknowledged after R1 and R2 confirm (W=2). R3 still has \"v1\".");

        // Read queries R=2 replicas -- R2 and R3. Any 2-of-3 read set is
        // guaranteed to overlap the 2-of-3 write set, because 2+2 > 3.
        List<Replica> readQuorum = Arrays.asList(r2, r3);
        System.out.println("Read queries R2 and R3 (R=2).");
        for (Replica r : readQuorum) {
            System.out.println("  " + r.id + ": value=\"" + r.value + "\", version=" + r.version);
        }

        Replica result = QuorumReader.readLatest(readQuorum);
        System.out.println("QuorumReader picks the HIGHEST version among the responses: "
            + result.id + " -> value=\"" + result.value + "\"");

        System.out.println(result.value.equals("v2")
            ? "FIXED: the read correctly returned the latest write. R2 was guaranteed to be in the read "
              + "set (2-of-3 read and 2-of-3 write quorums out of the same 3 replicas must share at "
              + "least one member), and comparing versions across the queried set correctly picked it "
              + "over R3's stale response."
            : "Unexpected -- investigate.");
    }
}
