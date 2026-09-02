public class Main {
    public static void main(String[] args) {
        // The primary has already applied User A's write at LSN=5.
        int primaryLsn = 5;
        String primaryBio = "Staff Engineer, loves distributed systems";
        System.out.println("User A writes a new bio to the primary. Primary is now at LSN=" + primaryLsn + ".");

        // Round-robin read routing sends the very next read to whichever replica
        // is next in rotation, with no regard for who just wrote what.
        Replica r1 = new Replica("R1", 3, "Backend Engineer"); // hasn't caught up to LSN=5 yet

        System.out.println("User A immediately reads their own profile. Round-robin routes the read to " + r1.id
            + ", which has only replicated up to LSN=" + r1.replicatedLsn + ".");

        String readResult = r1.readBio();
        System.out.println("Bio returned: \"" + readResult + "\"");

        System.out.println(readResult.equals(primaryBio)
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: User A just wrote this exact bio and immediately read back the OLD "
              + "one. From A's perspective, their own write appears to have vanished -- it didn't; it "
              + "just hadn't reached the replica the read happened to land on yet.");
    }
}
