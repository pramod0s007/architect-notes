import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Replica r1 = new Replica("R1", "v1", 1);
        Replica r2 = new Replica("R2", "v1", 1);
        Replica r3 = new Replica("R3", "v1", 1);

        int N = 3, W = 2, R = 2;
        System.out.println("N=" + N + ", W=" + W + ", R=" + R + " (W+R=" + (W + R) + " > N=" + N + " -- numerically valid)");

        r1.value = "v2"; r1.version = 2;
        r2.value = "v2"; r2.version = 2;
        System.out.println("Write \"v2\" acknowledged after R1 and R2 confirm (W=2). R3 still has \"v1\".");

        // Same read quorum as the fix -- R2 and R3 -- but this time R3 happens to
        // respond FIRST (its network path was faster this round), and the reader
        // doesn't wait to compare versions; it just uses whichever arrived first.
        List<Replica> responsesInArrivalOrder = Arrays.asList(r3, r2);
        System.out.println("Read queries R2 and R3 (R=2) -- this time R3 responds first.");

        Replica result = FirstResponseReader.readFirst(responsesInArrivalOrder);
        System.out.println("Reader returns whichever response arrived first: "
            + result.id + " -> value=\"" + result.value + "\", version=" + result.version);

        System.out.println(result.value.equals("v1")
            ? "STILL FAILS: the read quorum DID include R2, which had the latest write -- the overlap "
              + "guarantee held exactly as promised. The read returned stale data anyway, because the "
              + "reader never compared versions across the responses it received; it just used the "
              + "fastest one to arrive. W+R>N guarantees the latest version is SOMEWHERE in the response "
              + "set. It does not deliver it automatically -- something still has to look at every "
              + "response and pick the highest version, every time, not just the quickest one."
            : "Unexpected -- investigate.");
    }
}
