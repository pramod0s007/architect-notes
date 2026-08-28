import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Never pushes anything, ever -- a post is just appended to its author's
// own list. This genuinely solves the celebrity write-cost problem: posting
// costs the same regardless of follower count. It was picked by walking
// through exactly that one scenario. Nobody walked through the other
// common one: a user who follows a lot of accounts.
class PureFanOutOnReadFeedService {
    private final Map<String, List<String>> following = new HashMap<>();  // userId -> who they follow
    private final Map<String, List<Post>> authorPosts = new HashMap<>();  // authorId -> their own posts

    void follow(String followerId, String authorId) {
        following.computeIfAbsent(followerId, k -> new ArrayList<>()).add(authorId);
    }

    void post(String authorId, String content) {
        authorPosts.computeIfAbsent(authorId, k -> new ArrayList<>()).add(new Post(authorId, content));
    }

    List<Post> feedFor(String userId) {
        List<Post> feed = new ArrayList<>();
        for (String followedId : following.getOrDefault(userId, new ArrayList<>())) {
            feed.addAll(authorPosts.getOrDefault(followedId, new ArrayList<>()));
        }
        return feed;
    }
}
