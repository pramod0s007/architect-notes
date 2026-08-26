public class Main {
    public static void main(String[] args) {
        Report data = new Report("Q3 revenue: 4.2M");
        ReportGenerator generator = new PdfReportGenerator();
        System.out.println(generator.generate(data));
        System.out.println("v1: one axis of variation (format). Inheritance is fine here.");
    }
}
