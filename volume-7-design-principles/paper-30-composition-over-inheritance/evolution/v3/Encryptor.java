class Encryptor implements OutputTransform {
    public String apply(String input) {
        return "[ENCRYPTED]" + input + "[/ENCRYPTED]";
    }
}
