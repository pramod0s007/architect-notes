// Compress, then encrypt. A second, separate class for the reverse order.
class CompressedEncryptedPdfReportGenerator extends CompressedPdfReportGenerator {
    @Override
    String generate(Report data) {
        String compressed = super.generate(data);
        return "[ENCRYPTED]" + compressed + "[/ENCRYPTED]";
    }
}
