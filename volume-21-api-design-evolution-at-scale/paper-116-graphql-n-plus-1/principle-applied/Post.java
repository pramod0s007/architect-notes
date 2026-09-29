/** A GraphQL-resolved entity: a post, which has a top-level field author that must be resolved separately. */
public class Post {
    public final String id;
    public final String title;
    public final String authorId;

    public Post(String id, String title, String authorId) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
    }
}
