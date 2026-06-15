// SRP — owned by the Platform / Ops team.
// Single responsibility: deliver rendered report content to a destination.
// Data fetching or formatting changes never touch this class.

public class ReportDeliveryService {

    public void emailReport(String recipient, String content) {
        System.out.println("[DeliveryService] Sending email to: " + recipient);
        System.out.println("[DeliveryService] Subject: Sales Report");
        System.out.println("[DeliveryService] Content preview (first 120 chars):");
        System.out.println("  " + content.substring(0, Math.min(120, content.length()))
                .replace("\n", " "));
        System.out.println("[DeliveryService] Email dispatched successfully.");
    }

    public void saveToStorage(String filename, String content) {
        // In production: write to S3 / Azure Blob / GCS
        System.out.println("[DeliveryService] Saving report to storage: " + filename);
        System.out.println("[DeliveryService] Content size: "
                + content.length() + " characters.");
        System.out.println("[DeliveryService] File saved at gs://reports-bucket/" + filename);
    }
}
