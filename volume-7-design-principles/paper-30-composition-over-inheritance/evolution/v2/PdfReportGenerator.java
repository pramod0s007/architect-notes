class PdfReportGenerator extends ReportGenerator {
    @Override
    String generate(Report data) {
        return "%PDF-1.4\n" + data.getContent() + "\n%%EOF";
    }
}
