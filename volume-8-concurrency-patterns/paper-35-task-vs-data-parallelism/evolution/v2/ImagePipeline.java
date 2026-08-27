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

    static String correctSequential(String raw) {
        return encode(watermark(resize(decode(raw))));
    }
}
