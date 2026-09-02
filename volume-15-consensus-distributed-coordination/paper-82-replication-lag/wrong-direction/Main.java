public class Main {
    public static void main(String[] args) {
        String primaryBio = "Staff Engineer, loves distributed systems";
        System.out.println("User A writes a new bio to the primary.");

        // The overcorrection: once a client writes, pin ALL of that client's
        // future reads to the primary, permanently for the session -- never
        // route them to a replica again. It solves the writer's own problem by
        // brute force, at the cost of the entire point of having replicas.
        System.out.println("User A's session is now pinned to the PRIMARY for all future reads.");
        String userAReadsOwnProfile = primaryBio; // pinned -- always reads the primary directly
        System.out.println("User A reads their own profile: \"" + userAReadsOwnProfile + "\" -- fresh, as expected.");

        // A completely different user, who never wrote anything, views the same
        // profile a moment later. Nothing pins THEIR read to anything -- normal
        // round-robin routing still applies, and lands on a lagging replica.
        Replica r1 = new Replica("R1", 3, "Backend Engineer");
        System.out.println();
        System.out.println("User B (a different person, never wrote anything) views User A's profile "
            + "moments later. Round-robin routes User B's read to " + r1.id + ", replicatedLsn=3.");
        String userBReadsAProfile = r1.readBio();
        System.out.println("User B sees: \"" + userBReadsAProfile + "\"");

        System.out.println();
        System.out.println(!userBReadsAProfile.equals(primaryBio)
            ? "STILL FAILS, FOR EVERYONE EXCEPT THE WRITER: pinning fixed User A's own read-your-writes "
              + "problem, at the cost of sending every one of A's future reads to the primary forever, "
              + "whether they need freshness or not. It did nothing for User B, who sees the exact same "
              + "stale data the original bug produced -- because the fix only ever protected the person "
              + "who wrote, not anyone else who cares that the write happened. Read-your-own-writes and "
              + "\"everyone sees a write promptly\" are different guarantees, and this only ever solved "
              + "the narrower one, at unnecessary cost."
            : "Unexpected -- investigate.");
    }
}
