// LSP violation: ReadOnlyDocument cannot honour the save() contract.
// Callers holding a Document reference have no way to know this will explode at runtime.
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

    @Override
    public void save() {
        // LSP violation: throws at runtime even though the interface promises save() works
        throw new UnsupportedOperationException("Read-only documents cannot be saved");
    }
}
