import java.util.List;

public class Main {
    public static void main(String[] args) {
        Report data = new Report("Q3 revenue: 4.2M");

        ReportGenerator encryptThenCompress = new ReportGenerator(
                new PdfFormat(), List.of(new Encryptor(), new Compressor()));
        ReportGenerator compressThenEncrypt = new ReportGenerator(
                new PdfFormat(), List.of(new Compressor(), new Encryptor()));

        System.out.println("encrypt-then-compress: " + encryptThenCompress.generate(data));
        System.out.println("compress-then-encrypt: " + compressThenEncrypt.generate(data));

        // 3rd concern arrives: one new class (Signer), zero doubling of existing classes.
        ReportGenerator signed = new ReportGenerator(
                new PdfFormat(), List.of(new Encryptor(), new Compressor(), new Signer()));
        System.out.println("+ signing:             " + signed.generate(data));

        // 2nd format arrives: one new class (CsvFormat), same transforms reused as-is.
        ReportGenerator csvEncrypted = new ReportGenerator(
                new CsvFormat(), List.of(new Encryptor()));
        System.out.println("CSV + encryption:      " + csvEncrypted.generate(data));

        System.out.println();
        System.out.println("v3: 4 independent concerns, 6 classes total. No doubling.");
    }
}
