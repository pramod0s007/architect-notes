public class Main {
    public static void main(String[] args) {
        // 3 replicas, all starting at version 1 with the same value.
        Replica r1 = new Replica("R1", "v1", 1);
        Replica r2 = new Replica("R2", "v1", 1);
        Replica r3 = new Replica("R3", "v1", 1);

        int N = 3, W = 1, R = 1;
        System.out.println("N=" + N + ", W=" + W + ", R=" + R + " (W+R=" + (W + R) + " <= N=" + N + ")");

        // Write is acknowledged as soon as W=1 replica confirms -- only R1.
        r1.value = "v2";
        r1.version = 2;
        System.out.println("Write \"v2\" acknowledged after only R1 confirms (W=1). R2 and R3 still have \"v1\".");

        // Read queries only R=1 replica -- happens to be R3, which never got the write.
        Replica readResult = r3;
        System.out.println("Read queries only R3 (R=1).");
        System.out.println("Read returned: value=\"" + readResult.value + "\", version=" + readResult.version);

        System.out.println(readResult.value.equals("v1")
            ? "CONFIRMED FAILURE: the write already succeeded and was acknowledged, but this read "
              + "returned stale data. The read quorum (just R3) and the write quorum (just R1) never "
              + "had to overlap, because W+R=2 does not exceed N=3."
            : "Unexpected -- investigate.");
    }
}
