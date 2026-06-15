// ReadOnlyDocument implements Document only — no save() method at all.
// The type system now accurately reflects what this object can do.
public class ReadOnlyDocument implements Document {

    private String title;
    private String content;

    public ReadOnlyDocument(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getContent() { return content; }
}
