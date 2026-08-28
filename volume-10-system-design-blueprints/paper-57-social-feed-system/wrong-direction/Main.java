public class Main {
    private static final int REFRESHES_PER_SESSION = 50; // a realistic number of feed reloads in one app session

    public static void main(String[] args) {
        // The scenario this architecture WAS checked against: a celebrity posting.
        PureFanOutOnReadFeedService celebrityTest = new PureFanOutOnReadFeedService();
        for (int i = 0; i < 1_000_000; i++) {
            celebrityTest.follow("follower" + i, "celebrity");
        }
        long postStart = System.nanoTime();
        celebrityTest.post("celebrity", "hello from a very popular account");
        long postElapsedMs = (System.nanoTime() - postStart) / 1_000_000;
        System.out.println("Celebrity post with 1,000,000 followers: " + postElapsedMs + "ms (looks solved)");

        // The scenario nobody walked through: a user who follows a LOT of accounts,
        // reloading their feed a realistic number of times in one session.
        for (int followingCount : new int[]{10, 5_000, 200_000}) {
            PureFanOutOnReadFeedService service = new PureFanOutOnReadFeedService();
            for (int i = 0; i < followingCount; i++) {
                String friendId = "friend" + i;
                service.follow("powerUser", friendId);
                service.post(friendId, "a post");
            }

            long readStart = System.nanoTime();
            int feedSize = 0;
            for (int r = 0; r < REFRESHES_PER_SESSION; r++) {
                feedSize = service.feedFor("powerUser").size();
            }
            long readElapsedMs = (System.nanoTime() - readStart) / 1_000_000;

            System.out.printf("following=%,7d accounts : %d feed reloads took %dms total, feedSize=%d%n",
                    followingCount, REFRESHES_PER_SESSION, readElapsedMs, feedSize);
        }
    }
}
