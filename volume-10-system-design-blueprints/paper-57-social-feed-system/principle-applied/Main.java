public class Main {
    public static void main(String[] args) {
        for (int followerCount : new int[]{10, 5_000, 1_000_000}) {
            HybridFeedService service = new HybridFeedService();
            for (int i = 0; i < followerCount; i++) {
                service.follow("follower" + i, "celebrity");
            }

            long start = System.nanoTime();
            service.post("celebrity", "hello from a very popular account");
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("celebrity followers=%,9d : one post took %dms%n", followerCount, elapsedMs);
        }

        // A normal user follows 500 accounts, including 2 celebrities.
        HybridFeedService service = new HybridFeedService();
        for (int i = 0; i < 500; i++) {
            service.follow("normalUser", "friend" + i);
        }
        service.follow("normalUser", "celebrityA");
        service.follow("normalUser", "celebrityB");
        for (int i = 0; i < 20_000; i++) {
            service.follow("f" + i, "celebrityA"); // give celebrityA enough followers to cross the threshold
        }
        service.post("celebrityA", "celebrity post");

        long readStart = System.nanoTime();
        int feedSize = service.feedFor("normalUser").size();
        long readElapsedMs = (System.nanoTime() - readStart) / 1_000_000;

        System.out.println("Normal user following 500 accounts (2 celebrities): feed read took "
                + readElapsedMs + "ms, feedSize=" + feedSize);
    }
}
