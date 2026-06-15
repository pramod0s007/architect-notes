// The instanceof check is the symptom — it exists because the abstraction is broken.
// Every new Document subtype that can't save forces another branch here.
public class ExportService {

    public void exportDocument(Document doc) {
        System.out.println("Exporting: " + doc.getTitle());
        System.out.println("Content: " + doc.getContent());

        // instanceof check required because ReadOnlyDocument violates the save() contract
        if (!(doc instanceof ReadOnlyDocument)) {
            doc.save();
        } else {
            System.out.println("Skipping save — document is read-only");
        }
    }

    public static void main(String[] args) {
        ExportService service = new ExportService();

        service.exportDocument(new EditableDocument("Budget Report", "Q1 numbers..."));
        System.out.println("---");
        service.exportDocument(new ReadOnlyDocument("Archived Policy", "Do not modify."));
    }
}
