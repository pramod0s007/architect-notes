class ImagePipeline {
    static String decode(String raw) {
        return "decoded(" + raw + ")";
    }

    static String resize(String decoded) {
        return "resized(" + decoded + ")";
    }

    static String watermark(String resized) {
        return "watermarked(" + resized + ")";
    }

    static String encode(String watermarked) {
        return "encoded(" + watermarked + ")";
    }

    static String process(String raw) {
        String decoded = decode(raw);
        String resized = resize(decoded);
        String watermarked = watermark(resized);
        return encode(watermarked);
    }
}
