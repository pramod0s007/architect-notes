public class Main {
    public static void main(String[] args) {
        String result = ImagePipeline.process("image1.raw");
        System.out.println(result);
        System.out.println("v1: sequential pipeline, one image. Correct, and fine at low volume.");
    }
}
