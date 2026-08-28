import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Below the threshold: push on write, same as before -- reads stay instant.
// Above the threshold (a "celebrity"): skip the push entirely; a normal
// user's feed read merges in each celebrity they follow at read time.
// Write cost for a celebrity's post stops depending on follower count.
class HybridFeedService {
    private static final int CELEBRITY_THRESHOLD = 10_000;

    private final Map<String, List<String>> followers = new HashMap<>();     // authorId -> followers
    private final Map<String, List<String>> following = new HashMap<>();     // userId -> who they follow
    private final Map<String, List<Post>> pushedFeeds = new HashMap<>();     // userId -> precomputed feed
    private final Map<String, List<Post>> celebrityPosts = new HashMap<>();  // celebrityId -> their recent posts

    void follow(String followerId, String authorId) {
        followers.computeIfAbsent(authorId, k -> new ArrayList<>()).add(followerId);
        following.computeIfAbsent(followerId, k -> new ArrayList<>()).add(authorId);
    }

    void post(String authorId, String content) {
        Post post = new Post(authorId, content);
        int followerCount = followers.getOrDefault(authorId, new ArrayList<>()).size();

        if (followerCount > CELEBRITY_THRESHOLD) {
            celebrityPosts.computeIfAbsent(authorId, k -> new ArrayList<>()).add(post);
        } else {
            for (String followerId : followers.getOrDefault(authorId, new ArrayList<>())) {
                pushedFeeds.computeIfAbsent(followerId, k -> new ArrayList<>()).add(post);
            }
        }
    }

    List<Post> feedFor(String userId) {
        List<Post> feed = new ArrayList<>(pushedFeeds.getOrDefault(userId, new ArrayList<>()));
        for (String followedId : following.getOrDefault(userId, new ArrayList<>())) {
            feed.addAll(celebrityPosts.getOrDefault(followedId, new ArrayList<>()));
        }
        return feed;
    }
}
