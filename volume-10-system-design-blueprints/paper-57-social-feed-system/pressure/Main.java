public class Main {
    public static void main(String[] args) {
        for (int followerCount : new int[]{10, 5_000, 1_000_000}) {
            FanOutOnWriteFeedService service = new FanOutOnWriteFeedService();
            for (int i = 0; i < followerCount; i++) {
                service.follow("follower" + i, "celebrity");
            }

            long start = System.nanoTime();
            service.post("celebrity", "hello from a very popular account");
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("followers=%,9d : one post took %dms to fan out%n", followerCount, elapsedMs);
        }
    }
}
