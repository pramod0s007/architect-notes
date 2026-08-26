public class Main {
    public static void main(String[] args) {
        Report data = new Report("Q3 revenue: 4.2M");

        System.out.println(new EncryptedCompressedPdfReportGenerator().generate(data));
        System.out.println(new CompressedEncryptedPdfReportGenerator().generate(data));

        System.out.println();
        System.out.println("v2: 2 concerns (encrypt, compress) already needed 4 classes.");
        System.out.println("Adding a 3rd concern (digital signing) doubles this tree to 8+,");
        System.out.println("and a 4th format (CSV) doubles the WHOLE thing again.");
    }
}
