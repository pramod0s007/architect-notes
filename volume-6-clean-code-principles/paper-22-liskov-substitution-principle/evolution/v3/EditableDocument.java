public class EditableDocument implements MutableDocument {

    private String title;
    private String content;

    public EditableDocument(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getContent() { return content; }

    @Override
    public void save() {
        System.out.println("Saving document: " + title);
    }
}
