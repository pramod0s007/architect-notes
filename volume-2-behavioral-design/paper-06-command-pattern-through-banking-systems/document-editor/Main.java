/**
 * Run: javac *.java && java Main
 *
 * Demonstrates insert, delete, format — then undo/redo to show reversibility.
 */
public final class Main {

    public static void main(String[] args) {
        Document doc = new Document();
        EditorHistory history = new EditorHistory();

        System.out.println("=== Editing session ===");
        print(doc);

        // Step 1: type the headline
        history.execute(new InsertTextCommand(doc, 0, "Hello World"));
        print(doc);

        // Step 2: insert a comma
        history.execute(new InsertTextCommand(doc, 5, ","));
        print(doc);

        // Step 3: bold the word "Hello,"
        history.execute(new FormatTextCommand(doc, 0, 6, "bold"));
        System.out.println("  formats: " + doc.getActiveFormats());

        // Step 4: delete the comma
        history.execute(new DeleteTextCommand(doc, 5, 1));
        print(doc);

        System.out.println();
        System.out.println("=== Undo last 2 operations ===");
        history.undo(); // undo delete comma
        print(doc);
        history.undo(); // undo bold
        System.out.println("  formats: " + doc.getActiveFormats());

        System.out.println();
        System.out.println("=== Redo ===");
        history.redo(); // redo bold
        System.out.println("  formats: " + doc.getActiveFormats());
        history.redo(); // redo delete comma
        print(doc);

        System.out.println();
        System.out.println("=== Undo all the way to empty ===");
        while (history.canUndo()) {
            history.undo();
        }
        print(doc);
        history.undo(); // nothing left — should say so
    }

    private static void print(Document doc) {
        System.out.printf("  content: \"%s\"%n", doc.getContent());
    }
}
