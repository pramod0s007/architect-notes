// v3: LSP fixed — hierarchy restructured at capability boundary
// Compile note: Rectangle uses protected fields, accessible by Square in shape-area example
public class Main {
    public static void main(String[] args) {
        AutoSaveService autoSaver = new AutoSaveService();
        ExportService exporter = new ExportService();

        EditableDocument report = new EditableDocument("Q2 Report", "Revenue up 12%...");
        ReadOnlyDocument policy = new ReadOnlyDocument("Retention Policy", "Keep for 7 years.");

        // Both implement Document — both can be exported safely
        System.out.println("--- Export (works for all Documents) ---");
        exporter.exportDocument(report);
        exporter.exportDocument(policy);

        System.out.println("--- Auto-save (only MutableDocument) ---");
        autoSaver.autoSave(report);
        // autoSaver.autoSave(policy); // compile error — correct: ReadOnlyDocument is not MutableDocument
        System.out.println("Compiler enforces the contract. No instanceof checks needed.");
    }
}
