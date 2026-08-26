class Compressor implements OutputTransform {
    public String apply(String input) {
        return "[GZIP]" + input + "[/GZIP]";
    }
}
