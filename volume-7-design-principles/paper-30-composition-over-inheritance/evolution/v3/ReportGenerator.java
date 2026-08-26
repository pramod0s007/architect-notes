import java.util.List;

class ReportGenerator {
    private final ReportFormat format;
    private final List<OutputTransform> transforms;

    ReportGenerator(ReportFormat format, List<OutputTransform> transforms) {
        this.format = format;
        this.transforms = transforms;
    }

    String generate(Report data) {
        String output = format.render(data);
        for (OutputTransform t : transforms) {
            output = t.apply(output);
        }
        return output;
    }
}
