class EncryptedPdfReportGenerator extends PdfReportGenerator {
    @Override
    String generate(Report data) {
        String pdf = super.generate(data);
        return "[ENCRYPTED]" + pdf + "[/ENCRYPTED]";
    }
}
