class CompressedPdfReportGenerator extends PdfReportGenerator {
    @Override
    String generate(Report data) {
        String pdf = super.generate(data);
        return "[GZIP]" + pdf + "[/GZIP]";
    }
}
