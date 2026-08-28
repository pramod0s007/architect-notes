import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// When a post happens, immediately push it into every follower's own
// precomputed feed. Reads are then instant -- but the cost of ONE post
// scales with how many followers its author has.
class FanOutOnWriteFeedService {
    private final Map<String, List<String>> followers = new HashMap<>(); // authorId -> follower userIds
    private final Map<String, List<Post>> feeds = new HashMap<>();       // userId -> their precomputed feed

    void follow(String followerId, String authorId) {
        followers.computeIfAbsent(authorId, k -> new ArrayList<>()).add(followerId);
    }

    void post(String authorId, String content) {
        Post post = new Post(authorId, content);
        for (String followerId : followers.getOrDefault(authorId, new ArrayList<>())) {
            feeds.computeIfAbsent(followerId, k -> new ArrayList<>()).add(post);
        }
    }

    List<Post> feedFor(String userId) {
        return feeds.getOrDefault(userId, new ArrayList<>());
    }
}
