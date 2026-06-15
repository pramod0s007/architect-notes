// AutoSaveService operates on MutableDocument — the type that guarantees save() works.
// No defensive checks, no surprises. The contract is enforced at compile time.
public class AutoSaveService {

    public void autoSave(MutableDocument doc) {
        System.out.println("Auto-saving: " + doc.getTitle());
        doc.save();
    }

    public static void main(String[] args) {
        AutoSaveService autoSaver = new AutoSaveService();
        ExportService exporter = new ExportService();

        EditableDocument report = new EditableDocument("Q2 Report", "Revenue up 12%...");
        ReadOnlyDocument policy = new ReadOnlyDocument("Retention Policy", "Keep for 7 years.");

        // Both can be exported — Document contract is honoured by all subtypes
        exporter.exportDocument(report);
        exporter.exportDocument(policy);

        System.out.println("---");

        // Only MutableDocument can be auto-saved — compiler enforces this
        autoSaver.autoSave(report);
        // autoSaver.autoSave(policy); // compile error — ReadOnlyDocument is not MutableDocument
    }
}
