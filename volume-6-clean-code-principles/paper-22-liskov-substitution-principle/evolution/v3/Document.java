// v3: LSP fixed by removing save() from the base interface.
// Document promises only what every document can genuinely deliver.
public interface Document {
    String getTitle();
    String getContent();
}
