class CsvFormat implements ReportFormat {
    public String render(Report data) {
        return "content\n\"" + data.getContent() + "\"";
    }
}
