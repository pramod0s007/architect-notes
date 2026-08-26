// Encrypt, then compress. A DIFFERENT class from CompressedEncryptedPdfReportGenerator below —
// order matters, and inheritance can't express "same two concerns, different order" as one class.
class EncryptedCompressedPdfReportGenerator extends EncryptedPdfReportGenerator {
    @Override
    String generate(Report data) {
        String encrypted = super.generate(data);
        return "[GZIP]" + encrypted + "[/GZIP]";
    }
}
