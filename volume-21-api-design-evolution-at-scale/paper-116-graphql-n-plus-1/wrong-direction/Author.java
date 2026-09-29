/** A GraphQL-resolved entity: the author of a post. */
public class Author {
    public final String id;
    public final String name;

    public Author(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
