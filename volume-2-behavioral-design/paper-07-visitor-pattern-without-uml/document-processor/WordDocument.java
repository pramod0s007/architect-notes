/**
 * Concrete element: a Microsoft Word document.
 */
public final class WordDocument implements Document {

    private final String fileName;
    private final int wordCount;
    private final boolean hasTrackChanges;

    public WordDocument(String fileName, int wordCount, boolean hasTrackChanges) {
        this.fileName = fileName;
        this.wordCount = wordCount;
        this.hasTrackChanges = hasTrackChanges;
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public int getWordCount() {
        return wordCount;
    }

    public boolean hasTrackChanges() {
        return hasTrackChanges;
    }
}
