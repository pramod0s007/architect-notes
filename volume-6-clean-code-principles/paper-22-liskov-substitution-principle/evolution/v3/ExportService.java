// No instanceof check. Every Document can be exported safely.
// The compiler guarantees nothing unexpected happens here.
public class ExportService {

    public void exportDocument(Document doc) {
        System.out.println("Exporting: " + doc.getTitle());
        System.out.println("Content: " + doc.getContent());
    }

    public static void main(String[] args) {
        ExportService service = new ExportService();

        service.exportDocument(new EditableDocument("Budget Report", "Q1 numbers..."));
        service.exportDocument(new ReadOnlyDocument("Archived Policy", "Do not modify."));
    }
}
