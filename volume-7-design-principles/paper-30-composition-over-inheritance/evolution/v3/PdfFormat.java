class PdfFormat implements ReportFormat {
    public String render(Report data) {
        return "%PDF-1.4\n" + data.getContent() + "\n%%EOF";
    }
}
